Option Explicit

Public Const CONFIG_START_CELL = "D5"
Public Const TPL_CONTROL_BEG_CELL = "B2"

Type ImpBookDefine
    sImpName        As String
    sTplFileName    As String
    sXmlFileName    As String
    sTplId          As String
    sTplSheets      As String
    sNotImpSheets   As String
    sCopyFormula    As String
'    sExpFK         As String
End Type

Type TplConfigTitle
    id              As String   '_TBL_
    fillModel       As String   '区域填充类型
    beginCell       As String   '起始单元格
    endCell         As String   '结束单元格
    notNullCol      As String   '数据非空检查行/列
    titleBegRow     As String   '标题起始行(相对起始位)
    titleRowNum     As String   '标题行数
    dataBegRow      As String   '数据起始行(相对起始位)
    dataEndRow      As String   '数据结束行(相对结束位)
    tplRowNum       As String   '模板行数
    tplColumnNum    As String   '模板列数
    variable        As String   '缺省数据对象名
    sheetName       As String   '输出Sheet页名称
    tplSheet        As String   '模块Sheet页名称
End Type

Type AreaConfig
    id              As String   '_TBL_
    fillModel       As String   '区域填充类型
    beginCell       As String   '起始单元格
    endCell         As String   '结束单元格
    notNullCol      As String   '数据非空检查行/列
    titleBegRow     As Integer  '标题起始行(相对起始位)
    titleRowNum     As Integer  '标题行数
    dataBegRow      As Integer  '数据起始行(相对起始位)
    dataEndRow      As Integer  '数据结束行(相对结束位)
    tplRowNum       As Integer  '模板行数
    tplColumnNum    As Integer  '模板列数
    variable        As String   '缺省数据对象名
End Type

'错误种类    来源    错误描述    文件名  Sheet   Row Cell  单元格内容
Type LogHeader
    sFileName As String
    sSheetName As String
    sDBIOConf As String
    iRow As Integer
    sCell As String
    vCellValue As Variant
    
    sImpIsOk As String
    
    iAreaNum As Integer
    iImpReadNum As Integer
    iImpIgnoreNum As Integer
    iImpSuccNum As Integer
    iImpFailNum As Integer
    
    sNumber  As String
    sSource As String
    sDescription As String
End Type

Dim aaAreaConfigs(0 To 10, 12) As Variant '0: TplConfigTitle

'==============公共函数=========================
Sub ReadAreaConfigs(sheet As Worksheet, aaAreaConfigs As Variant)
    Dim iBegRow As Integer, iBegCol As Integer
    iBegRow = sheet.Range(TPL_CONTROL_BEG_CELL).Row
    iBegCol = sheet.Range(TPL_CONTROL_BEG_CELL).Column
    
    Dim iRow As Integer, iCol As Integer
    Dim iAreaSeq As Integer:  iAreaSeq = 0
    iCol = iBegCol + iAreaSeq
    
    Dim sId As String
    Do
        iCol = iBegCol + iAreaSeq
        sId = sheet.Cells(iBegRow, iCol).Value
        If sId = Empty Or sId = "" Then
            Exit Do
        End If
        
        Dim iRowSeq  As Integer
        For iRowSeq = 0 To 10
            iRow = iBegRow + iRowSeq
            If sheet.Cells(iRow, iCol).hasFormula Then
                Dim sFormula As String
                sFormula = sheet.Cells(iRow, iCol).Formula
                aaAreaConfigs(iAreaSeq, iRowSeq) = Right(sFormula, Len(sFormula) - 1)
            Else
                aaAreaConfigs(iAreaSeq, iRowSeq) = sheet.Cells(iRow, iCol).Value
            End If
        Next iRowSeq
        iAreaSeq = iAreaSeq + 1
    Loop
End Sub

Sub GetTplConfigTitle(aaAreaConfigs As Variant, iAreaSeq As Integer, areaCfg As TplConfigTitle)
    areaCfg.id = aaAreaConfigs(iAreaSeq, 0)                 '_TBL_
    areaCfg.fillModel = aaAreaConfigs(iAreaSeq, 1)         '区域填充类型
    areaCfg.beginCell = aaAreaConfigs(iAreaSeq, 2)         '起始单元格
    areaCfg.endCell = aaAreaConfigs(iAreaSeq, 3)           '结束单元格
    areaCfg.notNullCol = aaAreaConfigs(iAreaSeq, 4)        '数据非空检查行/列
    areaCfg.titleBegRow = aaAreaConfigs(iAreaSeq, 5)       '标题起始行(相对起始位)
    areaCfg.titleRowNum = aaAreaConfigs(iAreaSeq, 6)       '标题行数
    areaCfg.dataBegRow = aaAreaConfigs(iAreaSeq, 7)        '数据起始行(相对起始位)
    areaCfg.dataEndRow = aaAreaConfigs(iAreaSeq, 8)        '数据结束行(相对结束位)
    areaCfg.tplRowNum = aaAreaConfigs(iAreaSeq, 9)         '模板行数
    areaCfg.tplColumnNum = aaAreaConfigs(iAreaSeq, 10)     '模板列数
    areaCfg.variable = aaAreaConfigs(iAreaSeq, 11)         '数据变量名
    areaCfg.sheetName = aaAreaConfigs(iAreaSeq, 12)        '输出Sheet页名称
    areaCfg.tplSheet = areaCfg.id                          '模板Sheet页名称
End Sub

Sub GetAreaConfig(aaAreaConfigs As Variant, iAreaSeq As Integer, areaCfg As AreaConfig)
    areaCfg.id = aaAreaConfigs(iAreaSeq, 0)          '_TBL_
    areaCfg.fillModel = aaAreaConfigs(iAreaSeq, 1)         '区域填充类型
    areaCfg.beginCell = aaAreaConfigs(iAreaSeq, 2)         '起始单元格
    areaCfg.endCell = aaAreaConfigs(iAreaSeq, 3)           '结束单元格
    areaCfg.notNullCol = aaAreaConfigs(iAreaSeq, 4)        '数据非空检查行/列
    areaCfg.titleBegRow = aaAreaConfigs(iAreaSeq, 5)       '标题起始行(相对起始位)
    areaCfg.titleRowNum = aaAreaConfigs(iAreaSeq, 6)       '标题行数
    areaCfg.dataBegRow = aaAreaConfigs(iAreaSeq, 7)        '数据起始行(相对起始位)
    areaCfg.dataEndRow = aaAreaConfigs(iAreaSeq, 8)        '数据结束行(相对结束位)
    areaCfg.tplRowNum = aaAreaConfigs(iAreaSeq, 9)         '模板行数
    areaCfg.tplColumnNum = aaAreaConfigs(iAreaSeq, 10)     '模板列数
    areaCfg.variable = aaAreaConfigs(iAreaSeq, 11)         '数据变量名
End Sub
'==============公共函数 End=========================

