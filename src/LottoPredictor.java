import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

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

		System.out.println("predicting possible 5 sequences...");
		predictNextUniqueNumbers(winningNumbers, 3, 49, 1);
		predictNextUniqueNumbers(winningNumbers, 3, 49, 1);
		predictOffPreviousWinningNum(winningNumbers, 2, 49, 1);
		predictOffPreviousWinningNum(winningNumbers, 2, 49, 1);
		predictOffPreviousWinningNum(winningNumbers, 3, 49, 1);
		System.out.println("");
		final String uniqueNumbers = "U";
		final String similairNumbers = "S";
		final String highSequence = "H";
		final String lowSequence = "L";
		final String customSequence = "C";

		Scanner scan = new Scanner(System.in);
		System.out.println("");
		String input = "";

		while (!input.equalsIgnoreCase("quit")) {
			System.out.println("________________________________________________________");
			System.out.println("To predict unique numbers enter: " + uniqueNumbers);
			System.out.println("To predict simlair numbers to previous draws enter: " + similairNumbers);
			System.out.println("To predict low number sequence enter:  " + lowSequence);
			System.out.println("To predict high number sequence enter:  " + highSequence);
			System.out.println("To predict custom min to max number sequence enter:  " + customSequence);
			System.out.println("To quit enter: quit");
			input = scan.nextLine();
			if (input.equalsIgnoreCase(uniqueNumbers)) {
				System.out.println("Enter how many numbers you\'d like to be unique");
				System.out.println("i.e a number out of 7, i.e a number from the list [1, 2, 3, 4]");
				int number = Integer.parseInt(scan.nextLine());
				System.out.println("Generating winning sequence with " + (7 - number)
						+ " similair number('s) from all previous draws");
				predictNextUniqueNumbers(winningNumbers, (7 - number), 49, 1);
				System.out.println("");
			} else if (input.equalsIgnoreCase(similairNumbers)) {
				System.out.println("Enter how many numbers you\'d like to be similair to previous draw");
				System.out.print("i.e a number from the list [1, 2, 3]");
				int number = Integer.parseInt(scan.nextLine());
				System.out.println("Generating winning sequence with " + number
						+ " number('s) similair to previous winning draw:");
				predictOffPreviousWinningNum(winningNumbers, number, 49, 1);
				System.out.println("");
			} else if (input.equalsIgnoreCase(lowSequence)) {
				predictOffPreviousWinningNum(winningNumbers, 2, 24, 1);
			} else if (input.equalsIgnoreCase(highSequence)) {
				predictOffPreviousWinningNum(winningNumbers, 2, 25, 25);
			} else if (input.equalsIgnoreCase(customSequence)) {
				System.out.println("Enter min: ");
				int min = Integer.parseInt(scan.nextLine());
				System.out.println("Enter max: ");
				int max = Integer.parseInt(scan.nextLine());
				predictOffPreviousWinningNum(winningNumbers, 2, (max - min), min);
			}
		}

	}

	public static void predictNextUniqueNumbers(int winningNumbers[][], int similarities, int high, int low) {
		int[] newSequence = new int[7];

		newSequence = generateNewSequence(high, low);

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
				System.out.print("\t[");
				Arrays.sort(newSequence);
				for (int i = 0; i < 7; i++) {
					System.out.print(newSequence[i]);
					if (i < 7 - 1) {
						System.out.print(", ");
					}
				}
				Random r = new Random();
				System.out.println("] + [" + Math.round(r.nextFloat() * 49 + 1) + "]");
			} else {
				newSequence = generateNewSequence(high, low);
			}
		}

	}

//	
	public static void predictOffPreviousWinningNum(int winningNumbers[][], int simToPrevWeek, int high, int low) {
		int[] newSequence = new int[7];

		newSequence = generateNewSequence(high, low);

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
					System.out.print("\t[");
					Arrays.sort(newSequence);
					for (int i = 0; i < 7; i++) {
						System.out.print(newSequence[i]);
						if (i < 7 - 1) {
							System.out.print(", ");
						}
					}
					Random r = new Random();
					System.out.println("] + [" + Math.round(r.nextFloat() * 49 + 1) + "]");
				} else
					newSequence = generateNewSequence(high, low);

			} else
				newSequence = generateNewSequence(high, low);

		}

	}

	static int[] generateNewSequence(int high, int low) {
		Random r = new Random();
		int[] newSequence = new int[7];
		ArrayList<Integer> randomNumber = new ArrayList<Integer>();

		for (int i = 0; i < 7; i++) {
			int randomDigit = Math.round(r.nextFloat() * high + low);

			while (randomNumber.contains(randomDigit)) {
				randomDigit = Math.round(r.nextFloat() * high + low);
			}

			randomNumber.add(randomDigit);
			newSequence[i] = randomDigit;
		}

		return newSequence;
	}
}
