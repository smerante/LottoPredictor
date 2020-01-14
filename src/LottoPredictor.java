import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

//import org.apache.poi.ss.usermodel.Cell; 
//import org.apache.poi.ss.usermodel.CellStyle; 
//import org.apache.poi.ss.usermodel.DataFormat; 
//import org.apache.poi.ss.usermodel.Row; 
//import org.apache.poi.ss.usermodel.Sheet; 
//import org.apache.poi.ss.usermodel.Workbook;
//IMPORT ORG.APACHE.POI.HSSF.USERMODEL.HSSFCELL;
//import java.io.FileNotFoundException; 
//import java.io.FileOutputStream; 
//import java.util.Date;
public class LottoPredictor {

	public static void main(String[] args) {
		System.out.println("running");
		try {
			LottoPredictor.readFromExcel("lotto.xlsx", "Sheet1");
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void readFromExcel(String file, String sheet) throws IOException {
		XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(file));
		XSSFSheet myExcelSheet = myExcelBook.getSheet(sheet);

		for (int i = 0; i < myExcelSheet.getPhysicalNumberOfRows(); i++) {
			XSSFRow row = myExcelSheet.getRow(i);
			String name = row.getCell(2).getStringCellValue();
			System.out.println("cell : " + i + " : " + name);
		}


		myExcelBook.close();
	}
}
