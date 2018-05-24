import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.Map.Entry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Tester
{
	private static int totalCracked, newCracked, redundant;
	private static Map<String, String> hashedPasswords; // Hashed password to line number.
	private static Map<String, String> answers; // Line number to password.
	private static String[] hashedFileNames = new String[]{"weak.txt", "moderate.txt", "strong.txt"};
	private static String[] answerFileNames = new String[]{"answers_weak.txt", "answers_moderate.txt", "answers_strong.txt"};
	private static String[] numToPrefix = new String[]{"w", "m", "s"};
	private static String[] answerFileTitles = new String[]{"Weak\n", "Moderate\n", "Strong\n"};
	private static int[] deviationArray = new int[123];
	
	/*TODO:
	 *  Try 12GB list of passwords.
	 *  Dictionary words, joined together with:
	 *   - Numbers
	 *   - Random capitalizations of letters.
	 *  Brute-force short passwords (but random).
	 *  Take smaller common password lists and change things (like capitalization).
	 *  Keyboard patterns, numpad patterns.
	 */
	
	public static void main(String[] args)
	{
		int test = -1;
		try
		{
			test = Integer.parseInt(args[0]);
		}
		catch (Exception e)
		{
			System.out.println("Usage: 'java Tester n' where n is the test number.");
		}
		if (test == -1)
		{
			return;
		}
		
		if (test > 0)
		{
			hashedPasswords = fill();
			answers = readAnswers();
			totalCracked = answers.size();
			newCracked = 0;
			redundant = 0;
			
			deviationArray['s'] = '$';
			deviationArray['e'] = '3';
			deviationArray['a'] = '8';
			deviationArray['o'] = '0';
			deviationArray['l'] = '!';
		}
		
		/*  Tests:
		 */
		System.out.println("Running test " + test + "...\n");
		switch (test)
		{
		case -3:
		{
			System.out.println(MD5("password"));
			break;
		}
		case -5:
		{
			System.out.println(MD5("password"));
			System.out.println(MD5("Password"));
			break;
		}
		case -7:
		{
			StringBuffer a = new StringBuffer("1234");
			StringBuffer b = new StringBuffer(a);
			a.insert(1, "x");
			System.out.println(a);
			System.out.println(b);
			break;
		}
		case 1:
		{
			check("balloon");
			check("dog");
			break;
		}
		case 3:
		{
			check("password");
			check("rodent");
			break;
		}
		case 5:
		{
			check("alpine");
			check("blah");
			break;
		}
		case 7:
		{
			check("password");
			break;
		}
		case 9: // Try strings from other files. Use for merging multiple copies of originals.
		{
			for (int i = 1; i < args.length; i++)
			{
				tryWordsInFile(args[i]);
			}
			break;
		}
		case 11: // For merging.
		{
			main(new String[]{"9", "Merge.txt"});
			break;
		}
		case 13:
		{
			String[] words = getWords("CommonWords.txt", 650);
			tryTriples(words);
			break;
		}
		case 15:
		{
			String[] words = getWords("CommonWords.txt", 650);
			tryPairsWithNumbers(words);
			break;
		}
		case 17:
		{
			check(" ");
			check("  ");
			check(".");
			break;
		}
		case 19:
		{
			String[] words = getWords("CommonWords.txt", 650);
			tryPairsWithSymbols(words);
			break;
		}
		case 21:
		{
			for (int i = 0; i < 100000000; i++)
			{
				check (i + "");
			}
			break;
		}
		case 23:
		{
			String[] words = getWords("CommonWords.txt", 10000);
			tryPairs(words);
			break;
		}
		case 25:
		{
			String[] words = getWords("CommonWords.txt", 10000);
			tryPairsWithNumbers(words);
			break;
		}
		case 27:
		{
			for (long i = 0; i < 10000000000l; i++)
			{
				check (i + "");
				check ("0" + i);
			}
			break;
		}
		case 29:
		{
			String[] words = getWords("CommonWords.txt", 10000);
			tryDeviations(words, 4);
			break;
		}
		case 31:
		{
			String[] words = getWords("CommonWords.txt", 10000);
			tryPairDeviations(words, 1);
			break;
		}
		case 33:
		{
			String[] words = getWords("CommonWords.txt", 6000);
			tryPairDeviations(words, 2);
			break;
		}
		case 35:
		{
			String[] words = getWords("CommonWords.txt", 1000);
			tryPairDeviations(words, 3);
			break;
		}
		case 37:
		{
			for (int length = 1; length < 4; length++)
			{
				tryAllWordsOfLength(length);
			}
			break;
		}
		case 39:
		{
			tryAllWordsOfLength(4);
			break;
		}
		case 41:
		{
			tryAllWordsOfLength(5);
			break;
		}
		case 43:
		{
			for (int length = 1; length < 5; length++)
			{
				tryAlphaNumericWordsOfLength(length);
			}
			break;
		}
		case 45:
		{
			tryAlphaNumericWordsOfLength(6);
			break;
		}
		case 47:
		{
			tryDeviations(new String[]{"trombone"}, 3);
			break;
		}
		case 49: // Try strings from other files. Use for merging multiple copies of originals.
		{
			tryWordsInFile("Pastebin.txt");
			break;
		}
		}
		
		if (test > 0)
		{
			System.out.println("\nDiscovered " + newCracked + " new password(s), for a total of " + totalCracked + ".\nThere were " + redundant + " redundant checks.");
		}
		
		
	}
	
	/////////////////////////////////////
	//                                 //
	//  Functions that try passwords.  //
	//                                 //
	/////////////////////////////////////

	private static void tryWordsInFile(String fileName) // Checks all words in a file (separated by spaces and newlines).
	{
		try
		{
			Scanner in = new Scanner(new FileReader(fileName));
			while (in.hasNext())
			{
				String toCheck = in.next();
				check(toCheck);
			}
			in.close();
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	private static void tryWordsInFileAfterChar(String fileName, char c) // Checks all words in a file (separated by spaces and newlines).
	{
		try
		{
			Scanner in = new Scanner(new FileReader(fileName));
			while (in.hasNext())
			{
				String toCheck = in.next();
				check(toCheck.substring(toCheck.indexOf(c)));
			}
			in.close();
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	private static String[] getWords(String fileName, int numWords)
	{
		String[] toReturn = new String[numWords];
		try
		{
			Scanner in = new Scanner(new FileReader(fileName));
			for (int i = 0; i < numWords; i++)
			{
				String word = in.next();
				toReturn[i] = word;
			}
			in.close();
		}
		catch (Exception e)
		{
			System.out.println("Error: " + e.getMessage());
		}
		return toReturn;
	}
	
	private static void tryPairs(String[] words)
	{
		for (int i = 0; i < words.length; i++)
		{
			//System.out.println(i);
			for (int j = 0; j < words.length; j++)
			{
				String pair = words[i] + words[j];
				//System.out.println(pair);
				check(pair);
			}
		}
	}
	
	private static void tryTriples(String[] words)
	{
		for (int i = 0; i < words.length; i++)
		{
			for (int j = 0; j < words.length; j++)
			{
				for (int k = 0; k < words.length; k++)
				{
					String toCheck = words[i] + words[j] + words[k];
					check(toCheck);
				}
			}
		}
	}
	
	private static void tryPairsWithNumbers(String[] words)
	{
		for (int i = 0; i < words.length; i++)
		{
			for (int j = 0; j < words.length; j++)
			{
				for (int k = 0; k < 10; k++)
				{
					String toCheck = words[i] + k + words[j];
					check(toCheck);
					toCheck = words[i] + words[j] + k;
					check(toCheck);
					toCheck = words[i] + k + "" + k + words[j];
					check(toCheck);
					toCheck = words[i] + words[j] + k + "" + k;
					check(toCheck);
				}
			}
		}
	}
	
	private static void tryPairsWithSymbols(String[] words)
	{
		String[] symbols = new String[]{"!", "@", "#", "$", "%", "^", "&", "*", ">", "<", "~", "-"};
		for (int i = 0; i < words.length; i++)
		{
			for (int j = 0; j < words.length; j++)
			{
				for (String k : symbols)
				{
					String toCheck = k + words[i] + words[j];
					check(toCheck);
					toCheck = words[i] + k + words[j];
					check(toCheck);
					toCheck = words[i] + words[j] + k;
					check(toCheck);
				}
			}
		}
	}
	
	private static void tryDeviations(String[] words, int maxNumDeviations)
	{
		for (String word : words)
		{
			deviateRecursive(new StringBuffer(word), 0, maxNumDeviations);
		}
	}
	
	private static void tryPairDeviations(String[] words, int maxNumDeviations)
	{
		for (String word1 : words)
		{
			for (String word2 : words)
			{
				deviateRecursive(new StringBuffer(word1 + word2), 0, maxNumDeviations);
			}
		}
	}
	
	private static void deviateRecursive(StringBuffer s, int position, int deviations)
	{
		if (deviations <= 0 || position == s.length())
		{
			check(s.toString());
		}
		else
		{
			deviateRecursive(s, position + 1, deviations);
			char c = s.charAt(position);
			
			StringBuffer capitalized = new StringBuffer(s);
			capitalized.setCharAt(position, (char)(c - 32));
			deviateRecursive(capitalized, position + 1, deviations - 1);
			
			StringBuffer doubled = new StringBuffer(s);
			doubled.insert(position, c);
			deviateRecursive(doubled, position, deviations - 1);
			
			try
			{
				int deviation = deviationArray[c];
				if (deviation != 0)
				{
					StringBuffer newS = new StringBuffer(s);
					newS.setCharAt(position, (char)deviation);
					deviateRecursive(newS, position + 1, deviations - 1);
				}
			}
			catch (Exception e)
			{
				System.out.println("Error: " + e.getMessage());
			}
		}
	}
	
	private static void tryAllWordsOfLength(int length)
	{
		tryAllWordsOfLength(new StringBuffer(""), length);
	}
	
	private static void tryAllWordsOfLength(StringBuffer s, int lengthRemaining)
	{
		if (lengthRemaining == 0)
		{
			check(s.toString());
		}
		else
		{
			for (int i = 33; i <= 126; i++)
			{
				StringBuffer t = new StringBuffer(s);
				t.append((char)i);
				tryAllWordsOfLength(t, lengthRemaining - 1);
			}
		}
	}
	
	private static void tryAlphaNumericWordsOfLength(int length)
	{
		tryAlphaNumericWordsOfLength(new StringBuffer(""), length);
	}
	
	private static void tryAlphaNumericWordsOfLength(StringBuffer s, int lengthRemaining)
	{
		if (lengthRemaining == 0)
		{
			check(s.toString());
		}
		else
		{
			for (int i = 97; i <= 122; i++)
			{
				StringBuffer t = new StringBuffer(s);
				t.append((char)i);
				tryAllWordsOfLength(t, lengthRemaining - 1);
			}
			for (int i = 48; i <= 57; i++)
			{
				StringBuffer t = new StringBuffer(s);
				t.append((char)i);
				tryAllWordsOfLength(t, lengthRemaining - 1);
			}
		}
	}
	
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	//                                                                                  //
	//  Basic functions to handle reading and writing password lists to and from disk.  //
	//                                                                                  //
	//////////////////////////////////////////////////////////////////////////////////////
	
	private static String prefixToHashedFileName(String c)
	{
		if (c.equals("w"))
		{
			return "weak.txt";
		}
		else if (c.equals("m"))
		{
			return "moderate.txt";
		}
		else if (c.equals("s"))
		{
			return "strong.txt";
		}
		else
		{
			System.out.println("Unknown prefix: " + c);
			return "Error_dump.txt";
		}
	}
	
	private static Map<String, String> fill()
	{
		Map<String, String> toReturn = new HashMap<String, String>();
		for (int fileNum = 0; fileNum < 3; fileNum++)
		{
			String fileName = hashedFileNames[fileNum];
			String prefix = numToPrefix[fileNum];
			try
			{
				Scanner in = new Scanner(new FileReader(fileName));
				while (in.hasNext())
				{
					String[] line = in.nextLine().split(" ");
					if (line.length == 2)
					{
						toReturn.put(line[1], prefix + line[0]);
					}
				}
				in.close();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e.getMessage());
			}
		}
		return toReturn;
	}
	
	private static Map<String, String> readAnswers()
	{
		Map<String, String> toReturn = new HashMap<String, String>();
		for (int fileNum = 0; fileNum < 3; fileNum++)
		{
			String fileName = answerFileNames[fileNum];
			String prefix = numToPrefix[fileNum];
			try
			{
				Scanner in = new Scanner(new FileReader(fileName));
				while (in.hasNext())
				{
					String[] line = in.nextLine().split(" ");
					if (line.length == 2)
					{
						toReturn.put(prefix + line[0], line[1]);
					}
				}
				in.close();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e.getMessage());
			}
		}
		return toReturn;
	}
	
	private static String MD5(String s)
	{
		MessageDigest md;
		try
		{
			md = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e)
		{
			System.out.println("Error: " + e.getMessage());
			return "ERROR";
		}
		md.update(s.getBytes());
		byte byteData[] = md.digest();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++)
		{
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
	
	private static void check(String s)
	{
		//System.out.println("Checking: " + s);
		String hash = MD5(s);
		if (hashedPasswords.containsKey(hash))
		{
			String passwordLineNumber = hashedPasswords.get(hash);
			if (answers.containsKey(passwordLineNumber))
			{
				redundant++;
			}
			else
			{
				newCracked++;
				totalCracked++;
				answers.put(passwordLineNumber, s);
				System.out.println("(" + passwordLineNumber.charAt(0) + ") NEW PASSWORD: " + s);
				writeAnswers();
			}
		}
	}
	
	/*private static int labelToInt(String s)
	{
		return Integer.parseInt(s.substring(0, 2));
	}*/
	
	private static String intToLabel(int i)
	{
		if (i < 10)
		{
			return "0" + i + ".";
		}
		else
		{
			return i + ".";
		}
	}

	private static void writeAnswers()
	{
		for (int fileNum = 0; fileNum < 3; fileNum++)
		{
			String fileName = answerFileNames[fileNum];
			String prefix = numToPrefix[fileNum];
			PrintWriter writer;
			try
			{
				writer = new PrintWriter(fileName, "UTF-8");
			}
			catch (FileNotFoundException | UnsupportedEncodingException e)
			{
				System.out.println("Error:" + e.getMessage());
				return;
			}
			//for (Entry<Integer, String> e : originals.entrySet())
			writer.println(answerFileTitles[fileNum]);
			for (int i = 1; i <= 67; i++)
			{
				String label = intToLabel(i);
				String key = prefix + label;
				if (answers.containsKey(key))
				{
					writer.println(intToLabel(i) + " " + answers.get(key));
				}
				else
				{
					writer.println(intToLabel(i));
				}
			}
			writer.close();
		}
	}
}
