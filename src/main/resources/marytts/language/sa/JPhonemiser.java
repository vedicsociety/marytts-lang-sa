/*     */ package marytts.language.sa;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.StringTokenizer;
/*     */ import marytts.datatypes.MaryData;
/*     */ import marytts.datatypes.MaryDataType;
/*     */ import marytts.exceptions.MaryConfigurationException;
/*     */ import marytts.fst.FSTLookup;
/*     */ import marytts.language.hi.phonemiser.SanskritLTS;
/*     */ import marytts.modules.InternalModule;
/*     */ import marytts.modules.phonemiser.AllophoneSet;
/*     */ import marytts.server.MaryProperties;
/*     */ import marytts.util.MaryRuntimeUtils;
/*     */ import marytts.util.dom.MaryDomUtils;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.w3c.dom.DOMException;
/*     */ import org.w3c.dom.Document;
/*     */ import org.w3c.dom.Element;
/*     */ import org.w3c.dom.traversal.NodeIterator;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class JPhonemiser
/*     */   extends InternalModule
/*     */ {
/*     */   protected Map<String, List<String>> userdict;
/*     */   protected FSTLookup lexicon;
/*     */   protected SanskritLTS lts;
/*     */   protected AllophoneSet allophoneSet;
/*     */   
/*     */   public JPhonemiser(String propertyPrefix)
/*     */     throws IOException, MaryConfigurationException
/*     */   {
/*  72 */     this("JPhonemiser", MaryDataType.PARTSOFSPEECH, MaryDataType.PHONEMES, propertyPrefix + "allophoneset", propertyPrefix + "userdict", propertyPrefix + "utf8toit3map");
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public JPhonemiser(String componentName, MaryDataType inputType, MaryDataType outputType, String allophonesProperty, String userdictProperty, String utf8toit3mapProperty)
/*     */     throws IOException, MaryConfigurationException
/*     */   {
/*  92 */     super(componentName, inputType, outputType, MaryRuntimeUtils.needAllophoneSet(allophonesProperty).getLocale());
/*     */     
/*  94 */     this.allophoneSet = MaryRuntimeUtils.needAllophoneSet(allophonesProperty);
/*     */     
/*  96 */     String userdictFilename = MaryProperties.getFilename(userdictProperty);
/*  97 */     if (userdictFilename != null) {
/*  98 */       if (new File(userdictFilename).exists()) {
/*  99 */         this.userdict = readLexicon(userdictFilename);
/*     */       } else {
/* 101 */         this.logger.info("User dictionary '" + userdictFilename + "' for locale '" + getLocale() + "' does not exist. Ignoring.");
/*     */       }
/*     */     }
/* 104 */     InputStream utf8toit3mapStream = MaryProperties.needStream(utf8toit3mapProperty);
/* 105 */     this.lts = new SanskritLTS(utf8toit3mapStream);
/*     */   }
/*     */   
/*     */ 
/*     */   public MaryData process(MaryData d)
/*     */     throws Exception
/*     */   {
/* 112 */     Document doc = d.getDocument();
/* 113 */     NodeIterator it = MaryDomUtils.createNodeIterator(doc, doc, new String[] { "t" });
/* 114 */     Element t = null;
/* 115 */     while ((t = (Element)it.nextNode()) != null)
/*     */     {
/*     */ 
/*     */ 
/*     */ 
/* 120 */       if ((!t.hasAttribute("ph")) || (t.getAttribute("ph").contains("*")))
/*     */       {
/*     */         String text;
/*     */         String text;
/* 124 */         if (t.hasAttribute("sounds_like")) {
/* 125 */           text = t.getAttribute("sounds_like");
/*     */         } else {
/* 127 */           text = MaryDomUtils.tokenText(t);
/*     */         }
/* 129 */         String pos = null;
/*     */         
/* 131 */         if (t.hasAttribute("pos")) {
/* 132 */           pos = t.getAttribute("pos");
/*     */         }
/*     */         
/* 135 */         if ((text != null) && (!text.equals("")))
/*     */         {
/*     */ 
/*     */ 
/* 139 */           StringBuilder ph = new StringBuilder();
/* 140 */           String g2pMethod = null;
/* 141 */           StringTokenizer st = new StringTokenizer(text, " -");
/* 142 */           while (st.hasMoreTokens()) {
/* 143 */             String graph = st.nextToken();
/* 144 */             StringBuilder helper = new StringBuilder();
/* 145 */             if (!pos.equals("$PUNCT"))
/*     */             {
/*     */ 
/* 148 */               String phon = phonemise(graph, pos, helper);
/* 149 */               if (ph.length() == 0)
/*     */               {
/*     */ 
/* 152 */                 g2pMethod = helper.toString();
/* 153 */                 ph.append(phon);
/*     */               } else {
/* 155 */                 ph.append(" - ");
/*     */                 
/* 157 */                 ph.append(phon.replace('\'', ','));
/*     */               }
/*     */             }
/*     */           }
/* 161 */           if ((ph != null) && (ph.length() > 0)) {
/* 162 */             setPh(t, ph.toString());
/* 163 */             t.setAttribute("g2p_method", g2pMethod);
/*     */           }
/*     */         }
/*     */       } }
/* 167 */     MaryData result = new MaryData(outputType(), d.getLocale());
/* 168 */     result.setDocument(doc);
/* 169 */     return result;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String phonemise(String text, String pos, StringBuilder g2pMethod)
/*     */     throws IOException
/*     */   {
/* 191 */     String result = userdictLookup(text, pos);
/* 192 */     if (result != null) {
/* 193 */       g2pMethod.append("userdict");
/* 194 */       return result;
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */ 
/* 200 */     result = this.lts.phonemise(text);
/* 201 */     if (result != null) {
/* 202 */       g2pMethod.append("rules");
/* 203 */       return result;
/*     */     }
/*     */     
/* 206 */     return null;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String userdictLookup(String text, String pos)
/*     */   {
/* 223 */     if ((this.userdict == null) || (text == null) || (text.length() == 0)) return null;
/* 224 */     List<String> entries = (List)this.userdict.get(text);
/*     */     
/*     */ 
/*     */ 
/* 228 */     if (entries == null) {
/* 229 */       text = text.toLowerCase(getLocale());
/* 230 */       entries = (List)this.userdict.get(text);
/*     */     }
/* 232 */     if (entries == null) {
/* 233 */       text = text.substring(0, 1).toUpperCase(getLocale()) + text.substring(1);
/* 234 */       entries = (List)this.userdict.get(text);
/*     */     }
/*     */     
/* 237 */     if (entries == null) { return null;
/*     */     }
/* 239 */     String transcr = null;
/* 240 */     for (String entry : entries) {
/* 241 */       String[] parts = entry.split("\\|");
/* 242 */       transcr = parts[0];
/* 243 */       if ((parts.length > 1) && (pos != null)) {
/* 244 */         StringTokenizer tokenizer = new StringTokenizer(entry);
/* 245 */         while (tokenizer.hasMoreTokens()) {
/* 246 */           String onePos = tokenizer.nextToken();
/* 247 */           if (pos.equals(onePos)) { return transcr;
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 252 */     return transcr;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   protected Map<String, List<String>> readLexicon(String lexiconFilename)
/*     */     throws IOException
/*     */   {
/* 273 */     Map<String, List<String>> fLexicon = new HashMap();
/*     */     
/* 275 */     BufferedReader lexiconFile = new BufferedReader(new InputStreamReader(new FileInputStream(lexiconFilename), "UTF-8"));
/* 276 */     String line; while ((line = lexiconFile.readLine()) != null)
/*     */     {
/* 278 */       if ((!line.trim().equals("")) && (!line.startsWith("#")))
/*     */       {
/*     */ 
/* 281 */         String[] lineParts = line.split("\\s*\\|\\s*");
/* 282 */         String graphStr = lineParts[0];
/* 283 */         String phonStr = lineParts[1];
/*     */         try {
/* 285 */           this.allophoneSet.splitIntoAllophones(phonStr);
/*     */         } catch (RuntimeException re) {
/* 287 */           this.logger.warn("Lexicon '" + lexiconFilename + "': invalid entry for '" + graphStr + "'", re);
/*     */         }
/* 289 */         String phonPosStr = phonStr;
/* 290 */         if (lineParts.length > 2) {
/* 291 */           String pos = lineParts[2];
/* 292 */           if (!pos.trim().equals("")) {
/* 293 */             phonPosStr = phonPosStr + "|" + pos;
/*     */           }
/*     */         }
/* 296 */         List<String> transcriptions = (List)fLexicon.get(graphStr);
/* 297 */         if (null == transcriptions) {
/* 298 */           transcriptions = new ArrayList();
/* 299 */           fLexicon.put(graphStr, transcriptions);
/*     */         }
/* 301 */         transcriptions.add(phonPosStr);
/*     */       } }
/* 303 */     lexiconFile.close();
/* 304 */     return fLexicon;
/*     */   }
/*     */   
/*     */ 
/*     */   protected void setPh(Element t, String ph)
/*     */   {
/* 310 */     if (!t.getTagName().equals("t")) {
/* 311 */       throw new DOMException((short)15, "Only t elements allowed, received " + t.getTagName() + ".");
/*     */     }
/*     */     
/* 314 */     if (t.hasAttribute("ph")) {
/* 315 */       String prevPh = t.getAttribute("ph");
/*     */       
/* 317 */       String newPh = prevPh.replaceFirst("\\*", ph);
/* 318 */       t.setAttribute("ph", newPh);
/*     */     } else {
/* 320 */       t.setAttribute("ph", ph);
/*     */     }
/*     */   }
/*     */ }


/* Location:              /Users/b.vanalderweireldt/Workspace/workspace-upwork/Martin/marytts_from_server/lib/marytts-lang-hi-5.2-SNAPSHOT.jar!/marytts/language/hi/JPhonemiser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */