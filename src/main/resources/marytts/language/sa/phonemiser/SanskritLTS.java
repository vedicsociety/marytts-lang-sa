/*      */ package marytts.language.hi.phonemiser;
/*      */ 
/*      */ import java.io.BufferedReader;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.InputStreamReader;
/*      */ import java.io.PrintStream;
/*      */ import java.nio.CharBuffer;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.Scanner;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class HindiLTS
/*      */ {
/*      */   private HashMap<String, String> UTF8toPhoneSymbols;
/*      */   private HashMap<String, String> UTF8toPhoneTypes;
/*      */   private ArrayList<String> listPhoneSym;
/*      */   private ArrayList<String> listPhoneTypes;
/*      */   private ArrayList<String> listConTypes;
/*      */   private ArrayList<String> utf8CharList;
/*      */   
/*      */   public HindiLTS(InputStream utf8toit3mapStream)
/*      */     throws IOException
/*      */   {
/*   52 */     loadPhoneSymbolsAndTypes(utf8toit3mapStream);
/*      */   }
/*      */   
/*      */   public String phonemise(String line) throws IOException
/*      */   {
/*   57 */     String[] words = line.split("\\s+");
/*   58 */     String results = "";
/*   59 */     for (int i = 0; i < words.length; i++)
/*      */     {
/*   61 */       results = results + phonemiseWord(words[i].trim()) + " ";
/*      */     }
/*      */     
/*   64 */     return results.trim();
/*      */   }
/*      */   
/*      */   private String phonemiseWord(String word)
/*      */     throws IOException
/*      */   {
/*   70 */     this.utf8CharList = readUTF8String(word);
/*   71 */     this.listPhoneSym = new ArrayList();
/*   72 */     this.listPhoneTypes = new ArrayList();
/*   73 */     this.listConTypes = new ArrayList();
/*      */     
/*   75 */     Iterator<String> listrun = this.utf8CharList.iterator();
/*   76 */     while (listrun.hasNext())
/*      */     {
/*   78 */       String utf8Char = (String)listrun.next();
/*   79 */       String phoneSymbol = (String)this.UTF8toPhoneSymbols.get(utf8Char);
/*   80 */       String phoneType = (String)this.UTF8toPhoneTypes.get(utf8Char);
/*   81 */       if (phoneSymbol == null) phoneSymbol = getAsciiChar(utf8Char);
/*   82 */       if (phoneType == null) phoneType = "#";
/*   83 */       this.listPhoneSym.add(phoneSymbol);
/*   84 */       this.listPhoneTypes.add(phoneType);
/*   85 */       if ("CON".endsWith(phoneType)) {
/*   86 */         this.listConTypes.add("U");
/*      */       }
/*      */       else {
/*   89 */         this.listConTypes.add("#");
/*      */       }
/*      */     }
/*      */     
/*      */ 
/*      */ 
/*   95 */     removeUnknownSymbols();
/*      */     
/*      */ 
/*      */ 
/*   99 */     schwaHandler();
/*      */     
/*  101 */     applySanscriptRuleset();
/*      */     
/*  103 */     removeHal();
/*  104 */     syllabify();
/*  105 */     putStressMark();
/*      */     
/*  107 */     return getStringfromArrayList(this.listPhoneSym);
/*      */   }
/*      */   
/*      */   private void applySanscriptRuleset()
/*      */   {
/*  112 */     for (int i = 0; i < this.listPhoneTypes.size(); i++) {
/*  113 */       String type = (String)this.listPhoneTypes.get(i);
/*  114 */       if (type.equals("CON"))
/*      */       {
/*  116 */         if (i == this.listPhoneTypes.size() - 1) {
/*  117 */           this.listPhoneSym.add("aa");
/*  118 */           this.listPhoneTypes.add("VOW");
/*      */         }
/*      */         else {
/*  121 */           String nextType = (String)this.listPhoneTypes.get(i + 1);
/*  122 */           if (nextType.equals("CON")) {
/*  123 */             this.listPhoneSym.add(i + 1, "aa");
/*  124 */             this.listPhoneTypes.add(i + 1, "VOW");
/*  125 */             i++;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private ArrayList<String> putStressMark()
/*      */   {
/*  138 */     this.listPhoneSym.add(0, "'");
/*  139 */     return this.listPhoneSym;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */   private void syllabify()
/*      */   {
/*  147 */     for (int i = 0; i < this.listPhoneTypes.size(); i++) {
/*  148 */       if (isVowel(i)) {
/*  149 */         boolean isVowelLater = isVowelLater(i);
/*  150 */         boolean isNextSemiCon = isNextSemiConsonant(i);
/*  151 */         if (isVowelLater) {
/*  152 */           if (isNextSemiCon) {
/*  153 */             this.listPhoneSym.add(i + 2, "-");
/*  154 */             this.listPhoneTypes.add(i + 2, "SYM");
/*      */           }
/*      */           else {
/*  157 */             this.listPhoneSym.add(i + 1, "-");
/*  158 */             this.listPhoneTypes.add(i + 1, "SYM");
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private boolean isVowel(int pos)
/*      */   {
/*  171 */     if (((String)this.listPhoneTypes.get(pos)).equals("VOW")) {
/*  172 */       return true;
/*      */     }
/*  174 */     return false;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private boolean isVowelLater(int pos)
/*      */   {
/*  183 */     for (int i = pos + 1; i < this.listPhoneTypes.size(); i++) {
/*  184 */       if (((String)this.listPhoneTypes.get(i)).equals("VOW")) {
/*  185 */         return true;
/*      */       }
/*      */     }
/*  188 */     return false;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private boolean isNextSemiConsonant(int pos)
/*      */   {
/*  197 */     if (pos + 1 >= this.listPhoneSym.size()) return false;
/*  198 */     if ((((String)this.listPhoneSym.get(pos + 1)).equals("n:")) || (((String)this.listPhoneSym.get(pos + 1)).equals("a:"))) {
/*  199 */       return true;
/*      */     }
/*  201 */     return false;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private String getStringfromArrayList(ArrayList<String> aList)
/*      */   {
/*  211 */     Iterator<String> listrun = aList.iterator();
/*  212 */     StringBuilder result = new StringBuilder();
/*  213 */     while (listrun.hasNext())
/*      */     {
/*  215 */       result.append(" " + (String)listrun.next());
/*      */     }
/*  217 */     return result.toString();
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private String toHex4(int ch)
/*      */   {
/*  227 */     String hex = Integer.toHexString(ch).toUpperCase();
/*  228 */     switch (hex.length()) {
/*  229 */     case 3:  return "0" + hex;
/*  230 */     case 2:  return "00" + hex;
/*  231 */     case 1:  return "000" + hex; }
/*  232 */     return hex;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */   private void loadPhoneSymbolsAndTypes(InputStream inStream)
/*      */     throws IOException
/*      */   {
/*  240 */     BufferedReader bfr = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
/*  241 */     this.UTF8toPhoneSymbols = new HashMap();
/*  242 */     this.UTF8toPhoneTypes = new HashMap();
/*  243 */     String line; while ((line = bfr.readLine()) != null)
/*      */     {
/*  245 */       String[] words = line.split("\\|");
/*  246 */       this.UTF8toPhoneSymbols.put(words[0], words[1]);
/*  247 */       this.UTF8toPhoneTypes.put(words[0], words[2]);
/*      */     }
/*  249 */     bfr.close();
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private boolean isBelongs2KAVarga(String uniCodeHex)
/*      */   {
/*  260 */     int unicode = hexString2Int(uniCodeHex);
/*  261 */     int minFVChart = hexString2Int("0915");
/*  262 */     int maxFVChart = hexString2Int("0919");
/*      */     
/*  264 */     if ((unicode >= minFVChart) && (unicode <= maxFVChart)) {
/*  265 */       return true;
/*      */     }
/*  267 */     return false;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private boolean isBelongs2CHAVarga(String uniCodeHex)
/*      */   {
/*  276 */     int unicode = hexString2Int(uniCodeHex);
/*  277 */     int minFVChart = hexString2Int("091A");
/*  278 */     int maxFVChart = hexString2Int("091E");
/*      */     
/*  280 */     if ((unicode >= minFVChart) && (unicode <= maxFVChart)) {
/*  281 */       return true;
/*      */     }
/*  283 */     return false;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private boolean isBelongs2TAVarga(String uniCodeHex)
/*      */   {
/*  292 */     int unicode = hexString2Int(uniCodeHex);
/*  293 */     int minFVChart = hexString2Int("091F");
/*  294 */     int maxFVChart = hexString2Int("0923");
/*      */     
/*  296 */     if ((unicode >= minFVChart) && (unicode <= maxFVChart)) {
/*  297 */       return true;
/*      */     }
/*  299 */     return false;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private boolean isBelongs2THAVarga(String uniCodeHex)
/*      */   {
/*  308 */     int unicode = hexString2Int(uniCodeHex);
/*  309 */     int minFVChart = hexString2Int("0924");
/*  310 */     int maxFVChart = hexString2Int("0929");
/*      */     
/*  312 */     if ((unicode >= minFVChart) && (unicode <= maxFVChart)) {
/*  313 */       return true;
/*      */     }
/*  315 */     return false;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private boolean isBelongs2PAVarga(String uniCodeHex)
/*      */   {
/*  324 */     int unicode = hexString2Int(uniCodeHex);
/*  325 */     int minFVChart = hexString2Int("092A");
/*  326 */     int maxFVChart = hexString2Int("092E");
/*      */     
/*  328 */     if ((unicode >= minFVChart) && (unicode <= maxFVChart)) {
/*  329 */       return true;
/*      */     }
/*  331 */     return false;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private boolean isBelongs2YAVarga(String uniCodeHex)
/*      */   {
/*  340 */     int unicode = hexString2Int(uniCodeHex);
/*  341 */     int minFVChart = hexString2Int("092F");
/*  342 */     int maxFVChart = hexString2Int("0939");
/*      */     
/*  344 */     if ((unicode >= minFVChart) && (unicode <= maxFVChart)) {
/*  345 */       return true;
/*      */     }
/*  347 */     return false;
/*      */   }
/*      */   
/*      */   public ArrayList<String> readUTF8String(String word) throws IOException
/*      */   {
/*  352 */     CharBuffer cbuf = CharBuffer.wrap(word);
/*  353 */     ArrayList<String> utf8CharList = new ArrayList();
/*  354 */     for (int i = 0; i < cbuf.length(); i++) {
/*  355 */       char ch = cbuf.get(i);
/*  356 */       utf8CharList.add(toHex4(ch));
/*      */     }
/*  358 */     return utf8CharList;
/*      */   }
/*      */   
/*      */   public ArrayList<String> readUTF8File(String filename)
/*      */     throws IOException
/*      */   {
/*  364 */     ArrayList<String> utf8CharList = new ArrayList();
/*  365 */     InputStreamReader ins = new InputStreamReader(new FileInputStream(filename), "UTF8");
/*  366 */     int ch; while ((ch = ins.read()) >= 0) {
/*  367 */       utf8CharList.add(toHex4(ch));
/*      */     }
/*  369 */     return utf8CharList;
/*      */   }
/*      */   
/*      */   private void printData(String filename) throws IOException
/*      */   {
/*  374 */     ArrayList<String> utf8CharList = readUTF8File(filename);
/*      */     
/*  376 */     Iterator<String> listrun = utf8CharList.iterator();
/*  377 */     while (listrun.hasNext())
/*      */     {
/*  379 */       String utf8Char = (String)listrun.next();
/*  380 */       String phoneSymbol = (String)this.UTF8toPhoneSymbols.get(utf8Char);
/*  381 */       String phoneType = (String)this.UTF8toPhoneTypes.get(utf8Char);
/*  382 */       if (phoneSymbol == null) phoneSymbol = "SPACE";
/*  383 */       if (phoneType == null) phoneType = "#";
/*  384 */       System.out.println(utf8Char + " " + phoneSymbol + " " + phoneType);
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */   public void makeProperIt3(String filename)
/*      */     throws IOException
/*      */   {
/*  392 */     Scanner sc = new Scanner(new File(filename));
/*      */     
/*  394 */     while (sc.hasNextLine()) {
/*  395 */       String line = sc.nextLine().trim();
/*  396 */       String[] words = line.split("\\s+");
/*  397 */       for (int i = 0; i < words.length; i++) {
/*  398 */         System.out.println(words[i] + " --> " + phonemise(words[i].trim()));
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private ArrayList<String> removeHal(ArrayList<String> lPhoneSym, ArrayList<String> lPhoneTypes)
/*      */   {
/*  413 */     for (int i = 0; i < lPhoneTypes.size(); i++) {
/*  414 */       if (((String)lPhoneTypes.get(i)).equals("HLT")) {
/*  415 */         lPhoneTypes.remove(i);
/*  416 */         lPhoneSym.remove(i);
/*  417 */         i--;
/*      */       }
/*      */     }
/*  420 */     return lPhoneSym;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */   private void removeHal()
/*      */   {
/*  428 */     for (int i = 0; i < this.listPhoneTypes.size(); i++) {
/*  429 */       if (((String)this.listPhoneTypes.get(i)).equals("HLT")) {
/*  430 */         this.listPhoneTypes.remove(i);
/*  431 */         this.listPhoneSym.remove(i);
/*  432 */         i--;
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */   private void removeUnknownSymbols()
/*      */   {
/*  442 */     for (int i = 0; i < this.listPhoneTypes.size(); i++) {
/*  443 */       if (((String)this.listPhoneTypes.get(i)).equals("#")) {
/*  444 */         this.listPhoneTypes.remove(i);
/*  445 */         this.listPhoneSym.remove(i);
/*  446 */         i--;
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private String getAsciiChar(String utf8Char)
/*      */   {
/*  458 */     int intValue = Integer.parseInt(utf8Char, 16);
/*  459 */     char dec = (char)intValue;
/*  460 */     return Character.toString(dec);
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private void RuleSetZero()
/*      */   {
/*  472 */     boolean isFinalCharacter = false;
/*      */     
/*  474 */     for (int i = 0; i < this.listPhoneTypes.size(); i++)
/*      */     {
/*      */ 
/*      */ 
/*  478 */       String prevType = (String)this.listPhoneTypes.get(i);
/*  479 */       String prevPhone = (String)this.listPhoneSym.get(i);
/*  480 */       String prevUchar = (String)this.utf8CharList.get(i);
/*      */       String nextUchar;
/*  482 */       String nextType; String nextUchar; if (i + 1 < this.listPhoneTypes.size()) {
/*  483 */         String nextType = (String)this.listPhoneTypes.get(i + 1);
/*  484 */         String nextPhone = (String)this.listPhoneSym.get(i + 1);
/*  485 */         nextUchar = (String)this.utf8CharList.get(i + 1);
/*      */       } else {
/*  487 */         nextType = (String)this.listPhoneTypes.get(i);
/*  488 */         String nextPhone = (String)this.listPhoneSym.get(i);
/*  489 */         nextUchar = (String)this.utf8CharList.get(i);
/*  490 */         isFinalCharacter = true;
/*      */       }
/*      */       
/*      */ 
/*  494 */       if (prevUchar.equals("0902")) {
/*  495 */         if (isFinalCharacter == true) {
/*  496 */           this.listPhoneTypes.set(i, "CON");
/*  497 */           this.listPhoneSym.set(i, "ng~");
/*  498 */           this.utf8CharList.set(i, "0919");
/*  499 */           this.listConTypes.set(i, "U");
/*  500 */         } else if (isBelongs2TAVarga(nextUchar)) {
/*  501 */           this.listPhoneTypes.set(i, "CON");
/*  502 */           this.listPhoneSym.set(i, "n");
/*  503 */           this.utf8CharList.set(i, "0928");
/*  504 */           this.listConTypes.set(i, "U");
/*  505 */         } else if (isBelongs2PAVarga(nextUchar)) {
/*  506 */           this.listPhoneTypes.set(i, "CON");
/*  507 */           this.listPhoneSym.set(i, "m");
/*  508 */           this.utf8CharList.set(i, "092E");
/*  509 */           this.listConTypes.set(i, "U");
/*  510 */         } else if (isBelongs2KAVarga(nextUchar)) {
/*  511 */           this.listPhoneTypes.set(i, "CON");
/*  512 */           this.listPhoneSym.set(i, "ng~");
/*  513 */           this.utf8CharList.set(i, "0919");
/*  514 */           this.listConTypes.set(i, "U");
/*      */         }
/*      */       }
/*      */       
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*  524 */       if ((prevType.equals("CON")) && (nextType.equals("VOW"))) {
/*  525 */         this.listConTypes.set(i, "F");
/*  526 */       } else if (prevType.equals("VOW")) {
/*  527 */         this.listConTypes.set(i, "F");
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */   private void schwaHandler()
/*      */   {
/*  535 */     RuleSetZero();
/*      */     
/*  537 */     RuleSetOne();
/*      */     
/*  539 */     RuleSetTwo();
/*      */     
/*  541 */     RuleSetThree();
/*      */     
/*  543 */     RuleSetFour();
/*      */     
/*  545 */     RuleSetFive();
/*      */     
/*  547 */     RuleSetSix();
/*      */     
/*  549 */     RuleSetSeven();
/*      */     
/*  551 */     RuleSetEight();
/*      */     
/*      */ 
/*  554 */     FinalizeRules();
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */   private void FinalizeRules()
/*      */   {
/*  562 */     boolean isFinalCharacter = false;
/*  563 */     boolean encounterF = false;
/*      */     
/*  565 */     for (int i = 0; i < this.listPhoneTypes.size(); i++) {
/*  566 */       String prevType = (String)this.listPhoneTypes.get(i);
/*  567 */       String prevPhone = (String)this.listPhoneSym.get(i);
/*  568 */       String prevUchar = (String)this.utf8CharList.get(i);
/*  569 */       String prevCon = (String)this.listConTypes.get(i);
/*      */       String nextCon;
/*  571 */       String nextType; String nextUchar; if (i + 1 < this.listPhoneTypes.size()) {
/*  572 */         String nextType = (String)this.listPhoneTypes.get(i + 1);
/*  573 */         String nextPhone = (String)this.listPhoneSym.get(i + 1);
/*  574 */         String nextUchar = (String)this.utf8CharList.get(i + 1);
/*  575 */         nextCon = (String)this.listConTypes.get(i + 1);
/*      */       } else {
/*  577 */         nextType = (String)this.listPhoneTypes.get(i);
/*  578 */         String nextPhone = (String)this.listPhoneSym.get(i);
/*  579 */         nextUchar = (String)this.utf8CharList.get(i);
/*  580 */         String nextCon = (String)this.listConTypes.get(i);
/*  581 */         isFinalCharacter = true;
/*      */       }
/*      */       
/*      */ 
/*  585 */       if (isFinalCharacter == true) {
/*      */         break;
/*      */       }
/*      */       
/*  589 */       if (("F".equals(prevCon)) && ("CON".equals(prevType)) && (
/*  590 */         (!"VOW".equals(nextType)) || (isFullVowel(nextUchar)))) {
/*  591 */         this.listPhoneTypes.add(i + 1, "VOW");
/*  592 */         this.listPhoneSym.add(i + 1, "a");
/*  593 */         this.utf8CharList.add(i + 1, "093D");
/*  594 */         this.listConTypes.add(i + 1, "#");
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private void RuleSetTwo()
/*      */   {
/*  605 */     boolean isFinalCharacter = false;
/*      */     
/*  607 */     for (int i = 0; i < this.listPhoneTypes.size(); i++) {
/*  608 */       String prevType = (String)this.listPhoneTypes.get(i);
/*  609 */       String prevPhone = (String)this.listPhoneSym.get(i);
/*  610 */       String prevUchar = (String)this.utf8CharList.get(i);
/*  611 */       String prevCon = (String)this.listConTypes.get(i);
/*      */       String nextCon;
/*  613 */       String nextPhone; if (i + 1 < this.listPhoneTypes.size()) {
/*  614 */         String nextType = (String)this.listPhoneTypes.get(i + 1);
/*  615 */         String nextPhone = (String)this.listPhoneSym.get(i + 1);
/*  616 */         String nextUchar = (String)this.utf8CharList.get(i + 1);
/*  617 */         nextCon = (String)this.listConTypes.get(i + 1);
/*      */       } else {
/*  619 */         String nextType = (String)this.listPhoneTypes.get(i);
/*  620 */         nextPhone = (String)this.listPhoneSym.get(i);
/*  621 */         String nextUchar = (String)this.utf8CharList.get(i);
/*  622 */         String nextCon = (String)this.listConTypes.get(i);
/*  623 */         isFinalCharacter = true;
/*      */       }
/*      */       
/*      */ 
/*  627 */       if (isFinalCharacter == true) {
/*      */         break;
/*      */       }
/*      */       
/*      */ 
/*  632 */       if ("y".equals(nextPhone)) {
/*  633 */         if (("i".equals(prevPhone)) || ("ii".equals(prevPhone)) || ("u".equals(prevPhone)) || ("uu".equals(prevPhone))) {
/*  634 */           this.listConTypes.set(i + 1, "F");
/*  635 */         } else if ("U".equals(prevCon)) {
/*  636 */           this.listConTypes.set(i + 1, "F");
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */   private void RuleSetThree()
/*      */   {
/*  647 */     boolean isFinalCharacter = false;
/*      */     
/*  649 */     for (int i = 0; i < this.listPhoneTypes.size(); i++) {
/*  650 */       String prevType = (String)this.listPhoneTypes.get(i);
/*  651 */       String prevPhone = (String)this.listPhoneSym.get(i);
/*  652 */       String prevUchar = (String)this.utf8CharList.get(i);
/*  653 */       String prevCon = (String)this.listConTypes.get(i);
/*      */       String nextCon;
/*  655 */       String nextPhone; String nextCon; if (i + 1 < this.listPhoneTypes.size()) {
/*  656 */         String nextType = (String)this.listPhoneTypes.get(i + 1);
/*  657 */         String nextPhone = (String)this.listPhoneSym.get(i + 1);
/*  658 */         String nextUchar = (String)this.utf8CharList.get(i + 1);
/*  659 */         nextCon = (String)this.listConTypes.get(i + 1);
/*      */       } else {
/*  661 */         String nextType = (String)this.listPhoneTypes.get(i);
/*  662 */         nextPhone = (String)this.listPhoneSym.get(i);
/*  663 */         String nextUchar = (String)this.utf8CharList.get(i);
/*  664 */         nextCon = (String)this.listConTypes.get(i);
/*  665 */         isFinalCharacter = true;
/*      */       }
/*      */       
/*      */ 
/*  669 */       if (isFinalCharacter == true) {
/*      */         break;
/*      */       }
/*      */       
/*      */ 
/*  674 */       if (("U".equals(nextCon)) && (
/*  675 */         ("y".equals(nextPhone)) || ("r".equals(nextPhone)) || ("l".equals(nextPhone)) || ("v".equals(nextPhone)))) {
/*  676 */         if (("CON".equals(prevType)) && ("H".equals(prevCon))) {
/*  677 */           this.listConTypes.set(i + 1, "F");
/*  678 */         } else if ("HAL".equals(prevPhone)) {
/*  679 */           this.listConTypes.set(i + 1, "F");
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private void RuleSetOne()
/*      */   {
/*  692 */     boolean isFinalCharacter = false;
/*      */     
/*  694 */     for (int i = 0; i < this.listPhoneTypes.size(); i++) {
/*  695 */       String prevType = (String)this.listPhoneTypes.get(i);
/*  696 */       String prevPhone = (String)this.listPhoneSym.get(i);
/*  697 */       String prevUchar = (String)this.utf8CharList.get(i);
/*  698 */       String prevCon = (String)this.listConTypes.get(i);
/*      */       String nextCon;
/*  700 */       String nextType; if (i + 1 < this.listPhoneTypes.size()) {
/*  701 */         String nextType = (String)this.listPhoneTypes.get(i + 1);
/*  702 */         String nextPhone = (String)this.listPhoneSym.get(i + 1);
/*  703 */         String nextUchar = (String)this.utf8CharList.get(i + 1);
/*  704 */         nextCon = (String)this.listConTypes.get(i + 1);
/*      */       } else {
/*  706 */         nextType = (String)this.listPhoneTypes.get(i);
/*  707 */         String nextPhone = (String)this.listPhoneSym.get(i);
/*  708 */         String nextUchar = (String)this.utf8CharList.get(i);
/*  709 */         String nextCon = (String)this.listConTypes.get(i);
/*  710 */         isFinalCharacter = true;
/*      */       }
/*      */       
/*      */ 
/*  714 */       if (isFinalCharacter == true) {
/*      */         break;
/*      */       }
/*      */       
/*  718 */       if (("U".equals(prevCon)) && (prevType.equals("CON")) && (nextType.equals("HLT"))) {
/*  719 */         this.listConTypes.set(i, "H");
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private void RuleSetFour()
/*      */   {
/*  732 */     boolean isFinalCharacter = false;
/*      */     
/*  734 */     for (int i = 0; i < this.listPhoneTypes.size(); i++) {
/*  735 */       String prevType = (String)this.listPhoneTypes.get(i);
/*  736 */       String prevPhone = (String)this.listPhoneSym.get(i);
/*  737 */       String prevUchar = (String)this.utf8CharList.get(i);
/*  738 */       String prevCon = (String)this.listConTypes.get(i);
/*      */       String nextCon;
/*  740 */       String nextUchar; if (i + 1 < this.listPhoneTypes.size()) {
/*  741 */         String nextType = (String)this.listPhoneTypes.get(i + 1);
/*  742 */         String nextPhone = (String)this.listPhoneSym.get(i + 1);
/*  743 */         String nextUchar = (String)this.utf8CharList.get(i + 1);
/*  744 */         nextCon = (String)this.listConTypes.get(i + 1);
/*      */       } else {
/*  746 */         String nextType = (String)this.listPhoneTypes.get(i);
/*  747 */         String nextPhone = (String)this.listPhoneSym.get(i);
/*  748 */         nextUchar = (String)this.utf8CharList.get(i);
/*  749 */         String nextCon = (String)this.listConTypes.get(i);
/*  750 */         isFinalCharacter = true;
/*      */       }
/*      */       
/*      */ 
/*  754 */       if (isFinalCharacter == true) {
/*      */         break;
/*      */       }
/*      */       
/*  758 */       if (("U".equals(prevCon)) && (isFullVowel(nextUchar))) {
/*  759 */         this.listConTypes.set(i, "F");
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */   private void RuleSetFive()
/*      */   {
/*  769 */     boolean isFinalCharacter = false;
/*  770 */     boolean encounterF = false;
/*      */     
/*  772 */     for (int i = 0; i < this.listPhoneTypes.size(); i++) {
/*  773 */       String prevType = (String)this.listPhoneTypes.get(i);
/*  774 */       String prevPhone = (String)this.listPhoneSym.get(i);
/*  775 */       String prevUchar = (String)this.utf8CharList.get(i);
/*  776 */       String prevCon = (String)this.listConTypes.get(i);
/*      */       String nextCon;
/*  778 */       if (i + 1 < this.listPhoneTypes.size()) {
/*  779 */         String nextType = (String)this.listPhoneTypes.get(i + 1);
/*  780 */         String nextPhone = (String)this.listPhoneSym.get(i + 1);
/*  781 */         String nextUchar = (String)this.utf8CharList.get(i + 1);
/*  782 */         nextCon = (String)this.listConTypes.get(i + 1);
/*      */       } else {
/*  784 */         String nextType = (String)this.listPhoneTypes.get(i);
/*  785 */         String nextPhone = (String)this.listPhoneSym.get(i);
/*  786 */         String nextUchar = (String)this.utf8CharList.get(i);
/*  787 */         String nextCon = (String)this.listConTypes.get(i);
/*  788 */         isFinalCharacter = true;
/*      */       }
/*      */       
/*      */ 
/*  792 */       if (isFinalCharacter == true) {
/*      */         break;
/*      */       }
/*      */       
/*  796 */       if (("U".equals(prevCon)) && (!encounterF)) {
/*  797 */         this.listConTypes.set(i, "F");
/*      */       } else {
/*  799 */         if ("F".equals(prevCon)) {
/*      */           break;
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */   private void RuleSetSix()
/*      */   {
/*  810 */     boolean isFinalCharacter = false;
/*  811 */     boolean encounterF = false;
/*  812 */     int i = this.listPhoneTypes.size() - 1;
/*  813 */     if (i < 0) { return;
/*      */     }
/*      */     
/*  816 */     String prevType = (String)this.listPhoneTypes.get(i);
/*  817 */     String prevPhone = (String)this.listPhoneSym.get(i);
/*  818 */     String prevUchar = (String)this.utf8CharList.get(i);
/*  819 */     String prevCon = (String)this.listConTypes.get(i);
/*      */     
/*  821 */     if (("U".equals(prevCon)) && ("CON".equals(prevType))) {
/*  822 */       this.listConTypes.set(i, "H");
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private void RuleSetSeven()
/*      */   {
/*  833 */     boolean isFinalCharacter = false;
/*  834 */     boolean encounterF = false;
/*      */     
/*  836 */     for (int i = 0; i < this.listPhoneTypes.size(); i++) {
/*  837 */       String prevType = (String)this.listPhoneTypes.get(i);
/*  838 */       String prevPhone = (String)this.listPhoneSym.get(i);
/*  839 */       String prevUchar = (String)this.utf8CharList.get(i);
/*  840 */       String prevCon = (String)this.listConTypes.get(i);
/*      */       String nextCon;
/*  842 */       String nextType; String nextCon; if (i + 1 < this.listPhoneTypes.size()) {
/*  843 */         String nextType = (String)this.listPhoneTypes.get(i + 1);
/*  844 */         String nextPhone = (String)this.listPhoneSym.get(i + 1);
/*  845 */         String nextUchar = (String)this.utf8CharList.get(i + 1);
/*  846 */         nextCon = (String)this.listConTypes.get(i + 1);
/*      */       } else {
/*  848 */         nextType = (String)this.listPhoneTypes.get(i);
/*  849 */         String nextPhone = (String)this.listPhoneSym.get(i);
/*  850 */         String nextUchar = (String)this.utf8CharList.get(i);
/*  851 */         nextCon = (String)this.listConTypes.get(i);
/*  852 */         isFinalCharacter = true;
/*      */       }
/*      */       
/*      */ 
/*  856 */       if (isFinalCharacter == true) {
/*      */         break;
/*      */       }
/*      */       
/*  860 */       if (("U".equals(prevCon)) && ("CON".equals(prevType))) {
/*  861 */         if (("CON".equals(nextType)) && ("H".equals(nextCon))) {
/*  862 */           this.listConTypes.set(i, "F");
/*  863 */         } else if (("SYM".equals(nextType)) && ("#".equals(nextCon))) {
/*  864 */           this.listConTypes.set(i, "F");
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private void RuleSetEight()
/*      */   {
/*  876 */     boolean isFinalCharacter = false;
/*  877 */     boolean encounterF = false;
/*      */     
/*  879 */     for (int i = 1; i < this.listPhoneTypes.size() - 1; i++) {
/*  880 */       String prevType = (String)this.listPhoneTypes.get(i - 1);
/*  881 */       String prevPhone = (String)this.listPhoneSym.get(i - 1);
/*  882 */       String prevUchar = (String)this.utf8CharList.get(i - 1);
/*  883 */       String prevCon = (String)this.listConTypes.get(i - 1);
/*      */       
/*  885 */       String currentType = (String)this.listPhoneTypes.get(i);
/*  886 */       String currentPhone = (String)this.listPhoneSym.get(i);
/*  887 */       String currentUchar = (String)this.utf8CharList.get(i);
/*  888 */       String currentCon = (String)this.listConTypes.get(i);
/*      */       
/*  890 */       String nextType = (String)this.listPhoneTypes.get(i + 1);
/*  891 */       String nextPhone = (String)this.listPhoneSym.get(i + 1);
/*  892 */       String nextUchar = (String)this.utf8CharList.get(i + 1);
/*  893 */       String nextCon = (String)this.listConTypes.get(i + 1);
/*      */       
/*      */ 
/*  896 */       if (isFinalCharacter == true) {
/*      */         break;
/*      */       }
/*      */       
/*  900 */       if ("U".equals(currentCon)) {
/*  901 */         if (("F".equals(prevCon)) && ("F".equals(nextCon))) {
/*  902 */           this.listConTypes.set(i, "H");
/*      */         } else {
/*  904 */           this.listConTypes.set(i, "F");
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private void printArrayList(ArrayList<String> aList)
/*      */   {
/*  916 */     Iterator<String> listrun = aList.iterator();
/*  917 */     System.out.println();
/*  918 */     while (listrun.hasNext())
/*      */     {
/*      */ 
/*  921 */       System.out.print(" " + (String)listrun.next());
/*      */     }
/*  923 */     System.out.println();
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private boolean isFullVowel(String uniCodeHex)
/*      */   {
/*  933 */     int unicode = hexString2Int(uniCodeHex);
/*  934 */     int minFVChart = hexString2Int("0904");
/*  935 */     int maxFVChart = hexString2Int("0914");
/*      */     
/*  937 */     if ((unicode >= minFVChart) && (unicode <= maxFVChart)) {
/*  938 */       return true;
/*      */     }
/*  940 */     return false;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private String int2HexString(int ch)
/*      */   {
/*  950 */     String hex = Integer.toHexString(ch).toUpperCase();
/*  951 */     switch (hex.length()) {
/*  952 */     case 3:  return "0" + hex;
/*  953 */     case 2:  return "00" + hex;
/*  954 */     case 1:  return "000" + hex; }
/*  955 */     return hex;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   private int hexString2Int(String hexCode)
/*      */   {
/*  965 */     return Integer.parseInt(hexCode, 16);
/*      */   }
/*      */   
/*      */   private void printSchwaSequence() {
/*  969 */     if ((this.listPhoneSym.size() != this.listConTypes.size()) || (this.listPhoneSym.size() != this.utf8CharList.size()) || (this.listPhoneSym.size() != this.listPhoneTypes.size()))
/*      */     {
/*      */ 
/*  972 */       System.err.println(this.utf8CharList.size() + " " + this.listPhoneSym.size() + " " + this.listPhoneTypes.size() + " " + this.listConTypes.size());
/*      */       
/*  974 */       throw new RuntimeException("Array list sizes doesnot match !!!");
/*      */     }
/*  976 */     System.out.println("***************");
/*  977 */     for (int i = 0; i < this.utf8CharList.size(); i++) {
/*  978 */       System.out.println((String)this.utf8CharList.get(i) + " " + (String)this.listPhoneSym.get(i) + " " + (String)this.listPhoneTypes.get(i) + " " + (String)this.listConTypes.get(i));
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public static void main(String[] args)
/*      */     throws IOException
/*      */   {
/*  989 */     HindiLTS utf8r = new HindiLTS(new FileInputStream("/Users/sathish/Work/BitBucket/marytts/marytts-lang-hi/src/main/resources/marytts/language/hi/lexicon/UTF8toIT3.hi.list"));
/*      */     
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*  997 */     System.out.println("Result : " + utf8r.phonemise("आपका"));
/*  998 */     System.out.println("Result : " + utf8r.phonemise("बचपन"));
/*  999 */     System.out.println("Result : " + utf8r.phonemise("प्रियतम"));
/* 1000 */     System.out.println("Result : " + utf8r.phonemise("आमंत्रण"));
/* 1001 */     System.out.println("Result : " + utf8r.phonemise("कतई"));
/* 1002 */     System.out.println("Result : " + utf8r.phonemise("हूँ"));
/* 1003 */     utf8r.makeProperIt3("/Users/sathish/Work/BitBucket/delme/text/hin_0004.txt");
/*      */   }
/*      */ }


/* Location:              /Users/b.vanalderweireldt/Workspace/workspace-upwork/Martin/marytts_from_server/lib/marytts-lang-hi-5.2-SNAPSHOT.jar!/marytts/language/hi/phonemiser/HindiLTS.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */