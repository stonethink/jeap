package com.jeap.tools.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.jeecgframework.core.util.StringUtil;

//import org.jeecgframework.core.util.StringUtil;

public class JeapStringUtil {
	
	/**
	 * 判断是否是空字符串 null和"" 都返回 true
	 * 
	 * @author Stone
	 * @param s
	 * @return
	 */
	public static boolean isEmpty(String s) {
		if (s == null || s.equals("")) {
			return true;
		}else{
			return false;
		}
	}
	

	/**
	 * 自定义的分隔字符串函数 例如: 1,2,3 =>[1,2,3] 3个元素 ,2,3=>[,2,3] 3个元素 ,2,3,=>[,2,3,] 4个元素 ,,,=>[,,,] 4个元素
	 * 
	 * 5.22算法修改，为提高速度不用正则表达式 两个间隔符,,返回""元素
	 * 
	 * @param split
	 *            分割字符 默认,
	 * @param src
	 *            输入字符串
	 * @return 分隔后的list
	 * @author Robin
	 */
	public static List<String> splitToList(String split, String src) {
		// 默认,
		String sp = ",";
		if (split != null && split.length() == 1) {
			sp = split;
		}
		List<String> r = new ArrayList<String>();
		int lastIndex = -1;
		int index = src.indexOf(sp);
		if (-1 == index && src != null) {
			r.add(src);
			return r;
		}
		while (index >= 0) {
			if (index > lastIndex) {
				r.add(src.substring(lastIndex + 1, index));
			} else {
				r.add("");
			}

			lastIndex = index;
			index = src.indexOf(sp, index + 1);
			if (index == -1) {
				r.add(src.substring(lastIndex + 1, src.length()));
			}
		}
		return r;
	}
	
	public static String filePostfix(String fileName) {
		String postfix;
		
		if(JeapStringUtil.isEmpty(fileName)){
			postfix =  "";
		}else{
			int pos = fileName.lastIndexOf(".");
			if (pos > 0) {
				postfix = fileName.substring(pos + 1);
			} else {
				postfix =  "";
			}
		}
		return postfix;
	}
	
	public static String dbCode2Clazz(String sDbCode) {
		String sRtn = "";
		String[] strs = sDbCode.split("_");
		sRtn = "";
		int m = 0;
		for (int length = strs.length; m < length; m++) {
			if (m > 0) {
				String tempStr = strs[m].toLowerCase();
				tempStr = tempStr.substring(0, 1).toUpperCase()
						+ tempStr.substring(1, tempStr.length());
				sRtn = sRtn + tempStr;
			} else {
				sRtn = sRtn + strs[m].toLowerCase();
			}
		}
		return sRtn;
	}
	
	public static String dbCode2Property(String sDbCode) {
		String sRtn = "";
		String tempStr = dbCode2Clazz(sDbCode);
		sRtn = tempStr.substring(0, 1).toLowerCase()
				+ tempStr.substring(1, tempStr.length());
		return sRtn;
	}
	
	public static String regulatePackage(String packageName){
		String outPackage;
		if(JeapStringUtil.isEmpty(packageName)){
			outPackage = "";
		}else{
			//boolean isPoint = false;
			StringBuffer pkgSB = new StringBuffer();
			char preChar = '.';
			for(char c : packageName.toCharArray()){
				if(c == '.'){
					if(preChar == '.'){
						//Skip
					}else{
						pkgSB.append(c);
					}
				}else{
					if(c == ' '){
						//Skip
					}else{
						pkgSB.append(c);
					}
				}
				preChar = c;
			}
			int indexEnd = pkgSB.length() - 1;
			if(pkgSB.charAt(indexEnd) == '.'){
				pkgSB.deleteCharAt(indexEnd);
			}
			outPackage = pkgSB.toString();
		}
		
		return outPackage;
	}
	
/**
	public static String FormatDate(Date date, String sf)
	{
	  if (date == null) return "";
	  SimpleDateFormat dateformat = new SimpleDateFormat(sf);
	  return dateformat.format(date);
	}

	public static String dateToString(Date date)
	{
		if (date == null) return "";
		return FormatDate(date, "yyyy-MM-dd HH:mm:ss");
	}
**/

	// 截取数字
	public static String getNumbers(String content) {
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			return matcher.group(0);
		}
		return "";
	}

	// 截取非数字
	public static String splitNotNumber(String content) {
		Pattern pattern = Pattern.compile("\\D+");
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			return matcher.group(0);
		}
		return "";
	}
	
	public static List<String> splitByFlag(String inStr, String leftFlag, String rightFlag){
		List<String> paramsList = new ArrayList<String>();
		if (JeapStringUtil.isEmpty(inStr) 
				|| JeapStringUtil.isEmpty(leftFlag) 
				|| JeapStringUtil.isEmpty(rightFlag)){
			return paramsList;
		}
		int iStrLen = inStr.length();
		int ileftFlagLen = leftFlag.length();
		int iRightFlagLen = rightFlag.length();
		int iPos = 0;
		int iBeg = 0;
		int iEnd = 0;
		
		String tmpStr;
		String eleStr;
		while(iPos < iStrLen){
			tmpStr = inStr.substring(iPos);
			iBeg = tmpStr.indexOf(leftFlag);
			iEnd = tmpStr.indexOf(rightFlag);
			if(iBeg>=0 && iEnd>iBeg){
				eleStr = tmpStr.substring(iBeg + ileftFlagLen, iEnd);
				paramsList.add(eleStr);
				iPos = iPos + iEnd + iRightFlagLen;
			}else{
				break;
			}
		}
		
		return paramsList;
	}
	
	public static boolean checkStrSameAs( String inStr, String sameAs ){
		boolean isMatch = false;
		
		if(JeapStringUtil.isEmpty(inStr)){
			if(JeapStringUtil.isEmpty(sameAs)){
				return true;
			}else{
				return false;
			}
		}
		
		String regex = sameAs.replace("*", ".*");
		regex = "^" + regex + "$";
		
		try {
			Pattern pattern = Pattern.compile(regex);// 表达式
			Matcher matcher = pattern.matcher(inStr);// 要处理的内容
			
			if (matcher.find())// 如果匹配
			{
				isMatch = true;
			} else {// 如果不匹配
				isMatch = false;
			}
		} catch (Exception e) {
		}
		return isMatch;
	}
}
