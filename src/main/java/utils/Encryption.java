package utils;

public final class Encryption {

  public static String encryptDecryptXOR(String rawString) {

    // If encryption is enabled in Config.
    if (Config.getEncryption()) {

      // The key is predefined and hidden in code
      // TODO: Create a more complex code and store it somewhere better : FIX
      //The key is loaded form file and parsed to charArray (encryptionkey)
      char[] encryptionKey = Config.getENCRYPTIONKEY().toCharArray();

      // Stringbuilder enables you to play around with strings and make useful stuff
      StringBuilder thisIsEncrypted = new StringBuilder();

      // TODO: This is where the magic of XOR is happening. Are you able to explain what is going on? : FIX
      //Using the loop to go through the rawString (ex data shat should be encrypted)
      for (int i = 0; i < rawString.length(); i++) {
        //We look at the charvalue of i and take the binary value of this
        //and do xor with the bineary value of i % length of the encryptionkey
        //that will give us a bineary value that is then appended into the stringbuilder
        //we use xor to ensure that we dont exeed the length of our encryptionkey
        thisIsEncrypted.append((char) (rawString.charAt(i) ^ encryptionKey[i % encryptionKey.length]));
      }

      // We return the encrypted string
      return thisIsEncrypted.toString();

    } else {
      // We return without having done anything
      return rawString;
    }
  }
}
