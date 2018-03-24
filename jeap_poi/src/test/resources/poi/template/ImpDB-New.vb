'类的名称：数据导入操作
Option Explicit

Public Event OnError(ByVal Number As Long, ByVal Description As String, ByVal Source As String)
Public Event PreImpBook(ByRef wbWorkbook As Workbook)
Public Event PostImpBook(ByRef wbWorkbook As Workbook)
'Public Event PreImpSheet(ByRef wsWorkSheet As Worksheet, ByRef oImpDefine As ImpDBDefine)
'Public Event PostImpSheet(ByRef wsWorkSheet As Worksheet, ByRef oImpDefine As ImpDBDefine)

'Dim aaAreaConfigs(0 To 10, 10) As Variant '0: TplConfigTitle

'Dim tAreaConfig As AreaConfig

Dim tBookDefine As ImpBookDefine
Dim tTplConfigTitle As TplConfigTitle

Dim oFSO As Object

Dim sOutputFileName As String
'Dim fOutputFile As Integer
Dim fOutputFile As TextStream

Dim wsTplWorkSheet As Worksheet
'Dim wsWorkSheet As Worksheet

Private tLogHeader As LogHeader
'Private arLogRecordData As Variant
Private arLogBySheetRecordData() As Variant
Private arLogByRowRecordData() As Variant

Dim oLog As Log

Sub ImpData()
    On Error GoTo Error_ImpData
    
    SetImpLogExtHeader
    
   '导入数据
    Dim bIsOneFolder As Boolean: bIsOneFolder = True
    
    Dim sFileNameConf As String
    sFileNameConf = tBookDefine.sImpFileName
    
    Dim arrPreFolders() As String, iPreFoldersNum As Integer
    arrPreFolders = Split(sFileNameConf, "\")
    iPreFoldersNum = UBound(arrPreFolders)
    
    Dim sShortFileName As String
    sShortFileName = arrPreFolders(iPreFoldersNum)
    
    Dim sPrePath As String, iSeq As Integer
    If iPreFoldersNum > 0 Then
        For iSeq = 0 To iPreFoldersNum - 1
            If arrPreFolders(iSeq) = "*" Then
                bIsOneFolder = False
                Exit For
            Else
                sPrePath = sPrePath & "\" & arrPreFolders(iSeq)
            End If
        Next iSeq
    End If
    
    Set oFSO = CreateObject("Scripting.FileSystemObject")
    Dim sCurrPath As String: sCurrPath = ThisWorkbook.Path
    sCurrPath = sCurrPath & sPrePath
    Dim oCurrFolder As folder: Set oCurrFolder = oFSO.getfolder(sCurrPath)
    
    If bIsOneFolder = True Then
        ImpFilesInOneFolder oCurrFolder, sShortFileName
    Else
        ImpFilesInFolders oCurrFolder, sShortFileName
   End If
    
   Exit Sub
Error_ImpData:
    'oDBIOAccess.CloseConnection
    oLog.SysErrClear "Error_ImpData!"
End Sub

Private Sub ImpFilesInFolders(ByVal oCurrFolder As folder, sShortFileName As String)
    On Error GoTo Error_ImpFilesInFolders
    
    ImpFilesInOneFolder oCurrFolder, sShortFileName
    
    If oCurrFolder.subfolders.Count = 0 Then Exit Sub

    Dim oSubFolder As folder
    For Each oSubFolder In oCurrFolder.subfolders
        ImpFilesInFolders oSubFolder, sShortFileName
    Next oSubFolder
    
    Exit Sub
Error_ImpFilesInFolders:
    oLog.SysErr "Error_ImpFilesInFolders!"
End Sub

Private Sub ImpFilesInOneFolder(ByVal oCurrFolder As folder, sShortFileName As String)
    On Error GoTo Error_ImpFilesInOneFolder
    
    Dim sReadType As String
    Dim sPreFileName As String, sPostFileName As String
    
    Dim iPosStar As Integer
    If sShortFileName = "*" Then
        sReadType = "*"
    Else
        iPosStar = InStr(1, sShortFileName, "*")
        If iPosStar > 0 Then
            sReadType = "N"
            sPreFileName = Left(sShortFileName, iPosStar - 1)
            sPostFileName = Right(sShortFileName, Len(sShortFileName) - iPosStar)
        Else
            sReadType = "1"
        End If
    End If
    
    Dim oFile As File
    If sReadType = "1" Then
        ImpOneFile oFile, sReadType, oCurrFolder.Path, sShortFileName
    Else
        For Each oFile In oCurrFolder.Files
            ImpOneFile oFile, sReadType, sPreFileName, sPostFileName
        Next oFile
    End If
    
    Exit Sub
Error_ImpFilesInOneFolder:
    oLog.SysErr "Error_ImpFilesInOneFolder!"
End Sub

Private Sub ImpOneFile(ByVal oFile As File, sReadType As String, sPreFileName As String, sPostFileName As String)
    On Error GoTo Error_ImpOneFile
    InitFileLogHeader
   
    Dim sFullFileName As String
    Dim wbWorkbook As Workbook

    Select Case sReadType
        Case "1"
            sFullFileName = sPreFileName & "\" & sPostFileName
            tLogHeader.sFileName = sFullFileName
            If tBookDefine.sCopyFormula = "Y" Then
                Set wbWorkbook = Workbooks.Open(sFullFileName, 0, False)
            Else
                Set wbWorkbook = Workbooks.Open(sFullFileName, 0, True)
            End If
            If Not (wbWorkbook Is Nothing) Then
                ImpOneWorkBook wbWorkbook
                If tBookDefine.sCopyFormula = "Y" Then
                    wbWorkbook.Close SaveChanges:=True
                Else
                    wbWorkbook.Close SaveChanges:=False
               End If
            End If
        Case "N"
            Dim iPosShortFileName As Integer, iPreFileNameLen As Integer, iPostFileNameLen As Integer
            Dim sShortFileName As String
            If (oFSO.GetExtensionName(oFile) = "xlsx" Or oFSO.GetExtensionName(oFile) = "xls") Then
                sFullFileName = oFSO.GetFileName(oFile)
                iPreFileNameLen = Len(sPreFileName)
                iPostFileNameLen = Len(sPostFileName)
                iPosShortFileName = InStrRev(sFullFileName, "\")
                sShortFileName = Right(sFullFileName, Len(sFullFileName) - iPosShortFileName)
                If Len(sShortFileName) > iPreFileNameLen + iPostFileNameLen Then
                    If (Left(sShortFileName, iPreFileNameLen) = sPreFileName) And (Right(sShortFileName, iPostFileNameLen) = sPostFileName) Then
                        tLogHeader.sFileName = sFullFileName
                        If tBookDefine.sCopyFormula = "Y" Then
                            Set wbWorkbook = Application.Workbooks.Open(oFile, 0, False)
                        Else
                            Set wbWorkbook = Application.Workbooks.Open(oFile, 0, True)
                        End If
                        If Not (wbWorkbook Is Nothing) Then
                            ImpOneWorkBook wbWorkbook
                            If tBookDefine.sCopyFormula = "Y" Then
                                wbWorkbook.Close SaveChanges:=True
                            Else
                                wbWorkbook.Close SaveChanges:=False
                            End If
                        End If
                    End If
                End If
            End If
        Case "*"
            If (oFSO.GetExtensionName(oFile) = "xlsx" Or oFSO.GetExtensionName(oFile) = "xls") Then
                sFullFileName = oFSO.GetFileName(oFile)
                tLogHeader.sFileName = sFullFileName
                If tBookDefine.sCopyFormula = "Y" Then
                    Set wbWorkbook = Application.Workbooks.Open(oFile, 0, False)
                Else
                    Set wbWorkbook = Application.Workbooks.Open(oFile, 0, True)
                End If
                If Not (wbWorkbook Is Nothing) Then
                    ImpOneWorkBook wbWorkbook
                    If tBookDefine.sCopyFormula = "Y" Then
                        wbWorkbook.Close SaveChanges:=True
                    Else
                        wbWorkbook.Close SaveChanges:=False
                    End If
                End If
            End If
        Case Else
            '
    End Select
    
    Exit Sub
Error_ImpOneFile:
    If Not (wbWorkbook Is Nothing) Then
        wbWorkbook.Close SaveChanges:=False
    End If
    LogAppErr "Error_ImpOneFile"
End Sub

Private Sub ImpOneWorkBook(wbWorkbook As Workbook)
    On Error GoTo Error_ImpOneWorkBook
    
    Dim iSeq As Integer
    
    RaiseEvent PreImpBook(wbWorkbook)
    
    Dim sTmpFileName As String
    sTmpFileName = wbWorkbook.name
    
    If tImpBookDefine.sTplSheets = Empty Or tImpBookDefine.sTplSheets = "" Then
        Exit Sub
    End If

    Dim sCurrPath As String: sCurrPath = ThisWorkbook.Path
    If tImpBookDefine.sXmlFileName = Empty Or tImpBookDefine.sXmlFileName = "" Or tImpBookDefine.sXmlFileName = "*" Then
        sOutputFileName = sCurrPath & "\" & Left(sTmpFileName, Application.Find(".", sTmpFileName) - 1) & ".xml"
    Else
        sOutputFileName = tImpBookDefine.sXmlFileName
    End If
    
    Set fOutputFile = oFSO.CreateTextFile(sOutputFileName, True)
    
    Dim arrTplSheets() As String, iTplSheetsNum As Integer
    arrTplSheets = Split(tImpBookDefine.sTplSheets, ",")
    iTplSheetsNum = UBound(arrPreFolders)

    Dim sTplSheetName As String, iSeq As Integer
     
    For iSeq = 0 To iTplSheetsNum
        sTplSheetName = arrTplSheets(iSeq)
        If Not (sTplSheetName = Empty Or Trim(sTplSheetName) = "") Then
            Dim wsWorkSheet As Worksheet
    
            Set wsWorkSheet = wbWorkbook.Worksheets(Trim(sTplSheetName))
            
            DumpTblSheet wsWorkSheet
        End If
    Next iSeq

  
    RaiseEvent PostImpBook(wbWorkbook)
    
    'Close #fOutputFile
    fOutputFile.Close
    
    Exit Sub
Error_ImpOneWorkBook:
    LogAppErr "Error_ImpOneWorkBook"
End Sub


Private Sub DumpTblSheet(wsWorkSheet As Worksheet)
    InitSheetLogHeader
    
    ReadAreaConfigs wsWorkSheet, aaAreaConfigs
    Dim iAreaSeq As Integer: iAreaSeq = 0
    GetTplConfigTitle aaAreaConfigs, iAreaSeq, tTplConfigTitle
    
    If tTplConfigTitle.id = Empty Or tTplConfigTitle.id = "" Then
        Exit Sub
    End If
    
    DumpSheetHeader tTplConfigTitle
    
    Dim isEof As Boolean: isEof = False
    For iAreaSeq = 1 To UBound(aaAreaConfigs, 1)
        If aaAreaConfigs(iAreaSeq, 0) = Empty Or aaAreaConfigs(iAreaSeq, 0) = "" Then
            Exit For
        End If
        
        Dim tAreaConfig As AreaConfig
        GetAreaConfig aaAreaConfigs, iAreaSeq, tAreaConfig
        
        DumpOneArea wsWorkSheet, tAreaConfig
    Next iAreaSeq
    
    tLogHeader.sSheetName = wsWorkSheet.name
    
    LogImpInfoBySheet

Error_DumpTblSheet:
    LogAppErr "Error_DumpTblSheet"
End Sub

Private Sub DumpOneArea(wsWorkSheet As Worksheet, tAreaConfig As AreaConfig)
    Dim fillModle As String
    
    If tTplAreaConfig.fillModel = Empty Or tTplAreaConfig.fillModel = "" Then
        Exit Sub
    End If
    
    If UCase(tTplAreaConfig.fillModel) = "FIXED" Then
        DumpFixedOneArea wsWorkSheet, tAreaConfig
    Else
    If UCase(tTplAreaConfig.fillModel) = "ROW" Then
        DumpOneAreaByRow wsWorkSheet, tAreaConfig
    Else
        DumpOneAreaByColumn wsWorkSheet, tAreaConfig
    End If
    End If
    
    tLogHeader.iAreaNum = tLogHeader.iAreaNum + 1
End Sub

Private Sub DumpFixedOneArea(wsWorkSheet As Worksheet, tAreaConfig As AreaConfig)
    Dim iBegRow As Integer, iDataBegRow As Integer
    Dim iEndRow As Integer, iDataEndRow As Integer
    
    iBegRow = wsWorkSheet.Range(tAreaConfig.beginCell).Row
    iDataBegRow = iBegRow + tAreaConfig.dataBeginRow
    iEndRow = wsWorkSheet.Range(tAreaConfig.endCell).Row
    iDataEndRow = iEndRow + tAreaConfig.dataEndRow
    
    DumpAreaHeader tAreaConfig
    
    Dim iRow As Integer
    Dim sValue As String
    For iRow = iBegRow To iEndRow
        sValue = wsWorkSheet.Range(DDL_COL & iRow).Value
        If Not (sValue = Empty Or sValue = "") Then
            fOutputFile.WriteLine sValue
            tLogHeader.iImpSuccNum = tLogHeader.iImpSuccNum + 1
        Else
            tLogHeader.iImpIgnoreNum = tLogHeader.iImpIgnoreNum + 1
        End If
        tLogHeader.iImpReadNum = tLogHeader.iImpReadNum + 1
    Next iRow
    
    fOutputFile.WriteLine
End Sub

Private Sub DumpFixedOneArea(wsWorkSheet As Worksheet, tTplAreaConfig As AreaConfig, tAreaConfig As AreaConfig)
    Dim iBegRow As Integer, iDataBegRow As Integer
    Dim iEndRow As Integer, iDataEndRow As Integer
    
    iBegRow = wsWorkSheet.Range(tAreaConfig.beginCell).Row
    iDataBegRow = iBegRow + tAreaConfig.dataBeginRow
    iEndRow = wsWorkSheet.Range(tAreaConfig.endCell).Row
    iDataEndRow = iEndRow + tAreaConfig.dataEndRow
    
    Dim iRow As Integer
    Dim sValue As String
    For iRow = iBegRow To iEndRow
        sValue = wsWorkSheet.Range(DDL_COL & iRow).Value
        If Not (sValue = Empty Or sValue = "") Then
            fOutputFile.WriteLine sValue
            tLogHeader.iImpSuccNum = tLogHeader.iImpSuccNum + 1
        Else
            tLogHeader.iImpIgnoreNum = tLogHeader.iImpIgnoreNum + 1
        End If
        tLogHeader.iImpReadNum = tLogHeader.iImpReadNum + 1
    Next iRow
    
    fOutputFile.WriteLine
End Sub

Private Sub DumpSheetHeader()
'<?xml version="1.0" encoding="UTF-8"?>
'<Book id="tbls" notImportSheets="Notes,TBL_LIST,_*_">

    fOutputFile.WriteLine "<?xml version=""""1.0"""" encoding=""""UTF-8""""?>"
    Dim sValue As String
    
    sValue = "<Book"
    
    If Not (areaCfg.id = Empty Or areaCfg.id = "") Then
        sValue = sValue & " id = """ & areaCfg.id & """"
    End If

    If Not (areaCfg.variable = Empty Or areaCfg.variable = "") Then
        sValue = sValue & " variable = """ & areaCfg.variable & """"
    End If
    
    If Not (areaCfg.sheetName = Empty Or areaCfg.sheetName = "") Then
        sValue = sValue & " sheetName = """ & areaCfg.sheetName & """"
    End If
    
    If Not (areaCfg.tplSheet = Empty Or areaCfg.tplSheet = "") Then
        sValue = sValue & " tplSheet = """ & areaCfg.tplSheet & """"
    End If
    
    sValue = sValue & ">"
    fOutputFile.WriteLine sValue
End Sub

Private Sub DumpSheetHeader(areaCfg As TplConfigTitle)
'    <Sheet id="_LIST_" sheetName="TBL_LIST" tplSheet="_LIST_" >
    Dim sValue As String
    
    sValue = "    <Sheet"
    
    If Not (areaCfg.id = Empty Or areaCfg.id = "") Then
        sValue = sValue & " id = """ & areaCfg.id & """"
    End If

    If Not (areaCfg.variable = Empty Or areaCfg.variable = "") Then
        sValue = sValue & " variable = """ & areaCfg.variable & """"
    End If
    
    If Not (areaCfg.sheetName = Empty Or areaCfg.sheetName = "") Then
        sValue = sValue & " sheetName = """ & areaCfg.sheetName & """"
    End If
    
    If Not (areaCfg.tplSheet = Empty Or areaCfg.tplSheet = "") Then
        sValue = sValue & " tplSheet = """ & areaCfg.tplSheet & """"
    End If
    
    sValue = sValue & ">"
    fOutputFile.WriteLine sValue
End Sub

Private Sub DumpAreaHeader(areaCfg As AreaConfig)
'        <Area id="TL" fillModel="Dynamic" beginCell="B15" endCell="N15" variable = "$[entityList]{*org.jeap.devdb.entity.dev.TDevEntity}" titleRow="0" titleRowNum="1" dataBeginRow="2" tplRowNum="1" >
    Dim sValue As String
    
    sValue = "        <Area"
    
    If Not (areaCfg.id = Empty Or areaCfg.id = "") Then
        sValue = sValue & " id = """ & areaCfg.id & """"
    End If
    
    If Not (areaCfg.fillModel = Empty Or areaCfg.fillModel = "") Then
        sValue = sValue & " fillModel = """ & areaCfg.fillModel & """"
    End If
    
    If Not (areaCfg.beginCell = Empty Or areaCfg.beginCell = "") Then
        sValue = sValue & " beginCell = """ & areaCfg.beginCell & """"
    End If
    
    
    If Not (areaCfg.endCell = Empty Or areaCfg.endCell = "") Then
        sValue = sValue & " endCell = """ & areaCfg.endCell & """"
    End If
    
    If Not (areaCfg.notNullCol = Empty Or areaCfg.notNullCol = "") Then
        sValue = sValue & " notNullCol = """ & areaCfg.notNullCol & """"
    End If
    
    
    sValue = sValue & ">"
    fOutputFile.WriteLine sValue
End Sub

Private Sub LogImpInfoBySheet()
    Dim arLogDatas() As Variant
    Dim iLogDataNum As Integer
    iLogDataNum = 7 + UBound(arLogBySheetRecordData)
    
    ReDim arLogDatas(1 To iLogDataNum)
    
    arLogDatas(1) = tLogHeader.sFileName
    arLogDatas(2) = tLogHeader.sSheetName
    arLogDatas(3) = tLogHeader.iAreaNum
    arLogDatas(4) = tLogHeader.iImpReadNum
    arLogDatas(5) = tLogHeader.iImpIgnoreNum
    arLogDatas(6) = tLogHeader.iImpSuccNum
    arLogDatas(7) = tLogHeader.iImpFailNum
    
    Dim iSeq
    For iSeq = 1 To UBound(arLogBySheetRecordData)
        arLogDatas(7 + iSeq) = arLogBySheetRecordData(iSeq)
    Next iSeq
    
    oLog.LogImpLogByS arLogDatas
    
End Sub

Private Sub LogAppErr(sErrorFun As String)
    If Err.Number <> 0 Then
        tLogHeader.sNumber = Err.Number
        tLogHeader.sSource = Err.Source
        tLogHeader.sDescription = Err.Description
    End If
    
    oLog.LogErr tLogHeader.sNumber, sErrorFun & "-" & tLogHeader.sSource, tLogHeader.sDescription, _
                tLogHeader.sFileName, tLogHeader.sSheetName, tLogHeader.iRow, tLogHeader.sCell, tLogHeader.vCellValue
    Err.Clear
End Sub

Private Sub InitFileLogHeader()
    tLogHeader.sFileName = ""
    
    InitSheetLogHeader
End Sub

Private Sub InitSheetLogHeader()
    tLogHeader.sSheetName = ""
    tLogHeader.iAreaNum = 0
    tLogHeader.iRow = 0
    tLogHeader.iImpReadNum = 0
    tLogHeader.iImpIgnoreNum = 0
    tLogHeader.iImpSuccNum = 0
    tLogHeader.iImpFailNum = 0
    
    InitRowLogHeader
End Sub

Private Sub InitRowLogHeader()
    tLogHeader.sNumber = ""
    tLogHeader.sSource = ""
    tLogHeader.sDescription = ""
    
    tLogHeader.sCell = ""
    tLogHeader.vCellValue = ""
    tLogHeader.sImpIsOk = ""
End Sub

Private Sub SetImpLogExtHeader()
    ReDim arLogBySheetRecordData(0 To 0)
End Sub


Private Sub Class_Initialize()
    Set oLog = New Log
End Sub

'================= Property Begin ==========================
Public Property Let BookDefine(oPar As ImpBookDefine)
    tBookDefine = oPar
End Property

Public Property Get BookDefine() As ImpBookDefine
   BookDefine = tBookDefine
End Property
'================= Property End ==========================
