/*    */ package marytts.language.sa;
/*    */ 
/*    */ import marytts.config.LanguageConfig;
/*    */ import marytts.exceptions.MaryConfigurationException;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class SanskritConfig
/*    */   extends LanguageConfig
/*    */ {
/*    */   public SanskritConfig()
/*    */     throws MaryConfigurationException
/*    */   {
/* 28 */     super(SanskritConfig.class.getResourceAsStream("sa.config"));
/*    */   }
/*    */ }


/* Location:              /Users/b.vanalderweireldt/Workspace/workspace-upwork/Martin/marytts_from_server/lib/marytts-lang-hi-5.2-SNAPSHOT.jar!/marytts/language/hi/HindiConfig.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */