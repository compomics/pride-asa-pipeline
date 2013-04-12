package com.compomics.pride_asa_pipeline.model;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * This enum represents all the recognised amino acids. Note: this includes
 * common place holders like X for unknown AA B for Asn or Asp J for Leu or Ile
 * Z for Glu or Gln
 *
 * (reference: http://www.matrixscience.com/help/aa_help.html)
 *
 * @author Florian Reisinger Date: 30-Jul-2009
 * @since 0.1
 */
public enum AminoAcid {

    Ala("Alanine", 'A', 71.037114D),
    Arg("Arginine", 'R', 156.101111D),
    Asn("Asparagine", 'N', 114.042927D),
    Asp("Aspartic_Acid", 'D', 115.026943D),
    Cys("Cysteine", 'C', 103.009185D),
    Glu("Glutamic_Acid", 'E', 129.042593D),
    Gln("Glutamine", 'Q', 128.058578D),
    Gly("Glycine", 'G', 57.021464D),
    His("Histidine", 'H', 137.058912D),
    Ile("Isoleucine", 'I', 113.084064D),
    Xle("Isoleucine_or_Leucine", 'J', 113.084064D),
    Leu("Leucine", 'L', 113.084064D),
    Lys("Lysine", 'K', 128.094963D),
    Met("Methionine", 'M', 131.040485D),
    Phe("Phenylalanine", 'F', 147.068414D),
    Pro("Proline", 'P', 97.052764D),
    Ser("Serine", 'S', 87.032028D),
    Thr("Threonine", 'T', 101.047679D),
    SeC("Selenocysteine", 'U', 150.95363D),
    Trp("Tryptophan", 'W', 186.079313D),
    Tyr("Tyrosine", 'Y', 163.06332D),
    Val("Valine", 'V', 99.068414D),
    Pyl("Pyrrolysine", '0', 237.147727D),
    Asx("Asparagine_or_Aspartic_Acid", 'B', 0D),
    Glx("Glutamic_Acid_or_Glutamine", 'Z', 0D),
    Xaa("Unknown amino acid", 'X', 0D);
    
    private final String name;
    private final char letter;
    private final double mass;
    private static final HashMap<Character, AminoAcid> letterMap = new HashMap<Character, AminoAcid>();
    private static final HashMap<String, AminoAcid> nameMap = new HashMap<String, AminoAcid>();

    static {
        for (AminoAcid aa : EnumSet.allOf(AminoAcid.class)) {
            //make sure we only use upper case in the maps
            letterMap.put(Character.toUpperCase(aa.letter()), aa);
            nameMap.put(aa.fullName().toUpperCase(), aa);
        }
    }

    AminoAcid(String name, char letter, double mass) {
        this.name = name;
        this.letter = letter;
        this.mass = mass;
    }

    public String fullName() {
        return name;
    }

    public char letter() {
        return letter;
    }

    public double mass() {
        return mass;
    }

    @Override
    public String toString() {
        return name;
    }

    public static AminoAcid getAA(char c) {
        //in the maps we only use upper case
        return letterMap.get(Character.toUpperCase(c));
    }

    public static AminoAcid getAA(String letter) {
        return letterMap.get(getCharForString(letter));
    }

    public static boolean containsAA(char c) {
        //in the maps we only use upper case
        return letterMap.containsKey(Character.toUpperCase(c));
    }

    public static boolean containsAA(String letter) {
        //in the maps we only use upper case
        return letterMap.containsKey(getCharForString(letter));
    }

    public static AminoAcid getAAForName(String s) {
        //in the maps we only use upper case
        return nameMap.get(s.toUpperCase());
    }

    public static boolean containsAAByName(String s) {
        //in the maps we only use upper case
        return nameMap.containsKey(s.toUpperCase());
    }

    private static char getCharForString(String letter) {
        if (letter.length() != 1) {
            throw new IllegalArgumentException("Allowed values must have length 1! "
                    + "The provided letter did not match this criterium: " + letter);
        }
        return letter.toUpperCase().toCharArray()[0];
    }
}