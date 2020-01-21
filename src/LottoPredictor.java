import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import sun.misc.Regexp;

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

	static int winningNumbers[][];

	public static void main(String[] args) {
		System.out.println("running predictor...");
		try {
			LottoPredictor.readFromExcel("lotto.xlsx", "Sheet1");
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static ArrayList<String> stripNonDigits(final CharSequence input) {
		ArrayList<String> sequence = new ArrayList<String>();
		String numbers = "";
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (c > 47 && c < 58) {
				numbers = numbers + c;
			} else {
				boolean isNumber = true;
				try {
					Integer.parseInt(numbers);
				} catch (Exception e) {
					isNumber = false;
				}

				if (isNumber)
					sequence.add(numbers);

				numbers = "";
			}
		}
		sequence.add(numbers);
		return sequence;
	}

	public static void readFromExcel(String file, String sheet) throws IOException {
		XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(file));
		XSSFSheet myExcelSheet = myExcelBook.getSheet(sheet);
		int rows = myExcelSheet.getPhysicalNumberOfRows();
		winningNumbers = new int[rows - 1][7];
		for (int i = 1; i < rows; i++) {
			XSSFRow row = myExcelSheet.getRow(i);
			ArrayList<String> data = stripNonDigits(row.getCell(2).getStringCellValue());
			for (int j = 0; j < data.size(); j++) {
				try {
					winningNumbers[i - 1][j] = Integer.parseInt(data.get(j));
				} catch (Exception e) {
					System.out.println(e);
					System.out.println(
							"week " + (i + 1) + ", seq: " + data + " returned an error: \"" + data.get(j) + "\"");
				}
			}
		}

//		for (int i = 0; i < winningNumbers.length; i++) {
//			System.out.print("Week " + (i + 1) + ": [");
//			for (int j = 0; j < winningNumbers[i].length; j++) {
//				System.out.print(winningNumbers[i][j]);
//				if (j < winningNumbers[i].length - 1)
//					System.out.print(", ");
//			}
//			System.out.println("]");
//		}
		myExcelBook.close();
		System.out.println("predicting next winning unique numbers...");
		System.out.println("");
		
		predictNextUniqueNumbers(winningNumbers, 3);
		System.out.println("");
		
		predictNextSimilairNumbers(winningNumbers, 1);
		System.out.println("");
		
		predictNextSimilairNumbers(winningNumbers, 2);
		System.out.println("");

		System.out.println("Attempting to predict next winning unique numbers with less than 3 similarities...");
		System.out.println("");
		
		predictNextUniqueNumbers(winningNumbers, 2);
		
	}

	public static void predictNextUniqueNumbers(int winningNumbers[][], int similarities) {
		int[] newSequence = new int[7];

		newSequence = generateNewSequence();

		boolean previouslyDrawn = true;

		while (previouslyDrawn) {
			int maxSimilarNumbers = 0;

			// WeekI
			for (int weekI = 0; weekI < winningNumbers.length; weekI++) {
				int similarNumbersForRow = 0;

				// For each number drawn that week
				for (int numI = 0; numI < winningNumbers[weekI].length; numI++) {

					// Check every generated number
					for (int newGI = 0; newGI < 7; newGI++) {
						int currentNumber = newSequence[newGI];
						if (currentNumber == winningNumbers[weekI][numI]) {
							similarNumbersForRow++;
						}
					}
				}

//				if (similarNumbersForRow > maxSimilarNumbers)
//					System.out.println("Max similarities found: " + similarNumbersForRow + " in row: " + (weekI + 2)
//							+ " : " + winningNumbers[weekI][0] + "," + winningNumbers[weekI][1] + ","
//							+ winningNumbers[weekI][2] + "," + winningNumbers[weekI][3] + "," + winningNumbers[weekI][4]
//							+ "," + winningNumbers[weekI][5] + "," + winningNumbers[weekI][6]);
				maxSimilarNumbers = similarNumbersForRow > maxSimilarNumbers ? similarNumbersForRow : maxSimilarNumbers;
			}

			if (maxSimilarNumbers <= similarities) {
				previouslyDrawn = false;
				System.out.println("Sequence found has " + maxSimilarNumbers + " similarities to previous sequence: ");
				System.out.print("\t[");
				Arrays.sort(newSequence);
				for (int i = 0; i < 7; i++) {
					System.out.print(newSequence[i]);
					if (i < 7 - 1) {
						System.out.print(", ");
					}
				}
				System.out.println("]");
			} else {
				newSequence = generateNewSequence();
			}
		}

	}

	public static void predictNextSimilairNumbers(int winningNumbers[][], int simToPrevWeek) {
		int[] newSequence = new int[7];

		newSequence = generateNewSequence();

		boolean previouslyDrawn = true;

		while (previouslyDrawn) {
			int maxSimilarNumbers = 0;
			int maxSimilarNumbersToPrevWeek = 0;
			int lastDrawnWeek = 0;
			int simToLastDrawn = 0;
			// Check numbers for last drawn winning numbers
			for (int numI = 0; numI < 7; numI++) {

				// Check every generated number
				for (int newGI = 0; newGI < 7; newGI++) {
					int currentNumber = newSequence[newGI];
					if (currentNumber == winningNumbers[lastDrawnWeek][numI]) {
						simToLastDrawn++;
					}
				}
			}
			maxSimilarNumbersToPrevWeek = simToLastDrawn > maxSimilarNumbersToPrevWeek ? simToLastDrawn
					: maxSimilarNumbersToPrevWeek;

			if (maxSimilarNumbersToPrevWeek == simToPrevWeek) {

				// WeekI
				for (int weekI = 0; weekI < winningNumbers.length; weekI++) {
					int similarNumbersForRow = 0;

					// For each number drawn that week
					for (int numI = 0; numI < winningNumbers[weekI].length; numI++) {

						// Check every generated number
						for (int newGI = 0; newGI < 7; newGI++) {
							int currentNumber = newSequence[newGI];
							if (currentNumber == winningNumbers[weekI][numI]) {
								similarNumbersForRow++;
							}
						}
					}

					maxSimilarNumbers = similarNumbersForRow > maxSimilarNumbers ? similarNumbersForRow
							: maxSimilarNumbers;
				}
				if (maxSimilarNumbers <= 3) {
					previouslyDrawn = false;
//					5 17 19 25 31 38 46
					System.out.println("Unique sequence with " + maxSimilarNumbersToPrevWeek
							+ " similair numbers to last draw:");
					System.out.print("\t[");
					Arrays.sort(newSequence);
					for (int i = 0; i < 7; i++) {
						System.out.print(newSequence[i]);
						if (i < 7 - 1) {
							System.out.print(", ");
						}
					}
					System.out.println("]");
				} else
					newSequence = generateNewSequence();

			} else
				newSequence = generateNewSequence();

		}

	}

	static int[] generateNewSequence() {
		Random r = new Random(System.currentTimeMillis());
		int[] newSequence = new int[7];
		ArrayList<Integer> randomNumber = new ArrayList<Integer>();

		for (int i = 0; i < 7; i++) {
			int randomDigit = Math.round(r.nextFloat() * 49 + 1);

			while (randomNumber.contains(randomDigit)) {
				randomDigit = Math.round(r.nextFloat() * 49 + 1);
			}

			randomNumber.add(randomDigit);
			newSequence[i] = randomDigit;
		}

		return newSequence;
	}
}
