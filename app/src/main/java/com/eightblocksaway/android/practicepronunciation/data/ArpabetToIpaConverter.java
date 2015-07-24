package com.eightblocksaway.android.practicepronunciation.data;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ArpabetToIpaConverter {

    private static final Map<String, String> convertionMap;

    static {
        Map<String, String> mutableMap = new HashMap<>();

        /* Use search and replace with:
         "(\w+)" => ("(\S)+") to mutableMap.put("$1", $2)
          */

        /*
         Vowels - Monophthongs
		Arpabet	IPA		Word examples
		AO		ɔ		off (AO1 F); fall (F AO1 L); frost (F R AO1 S T)
		AA		ɑ		father (F AA1 DH ER), cot (K AA1 T)
		IY		i		bee (B IY1); she (SH IY1)
		UW		u		you (Y UW1); new (N UW1); food (F UW1 D)
		EH		ɛ OR e 	ed (R EH1 D); men (M EH1 N)
		IH		ɪ		big (B IH1 G); win (W IH1 N)
		UH		ʊ		should (SH UH1 D), could (K UH1 D)
		AH		ʌ		but (B AH1 T), sun (S AH1 N)
		AH(AH0) ə		sofa (S OW1 F AH0), alone (AH0 L OW1 N)
		AE		æ		at (AE1 T); fast (F AE1 S T)
		AX		ə 		discus (D IH1 S K AX0 S);
		*/
        mutableMap.put("AO", "ɔ");
        mutableMap.put("AO0", "ɔ");
        mutableMap.put("AO1", "ɔ");
        mutableMap.put("AO2", "ɔ");
        mutableMap.put("AA", "ɑ");
        mutableMap.put("AA0", "ɑ");
        mutableMap.put("AA1", "ɑ");
        mutableMap.put("AA2", "ɑ");
        mutableMap.put("IY", "i");
        mutableMap.put("IY0", "i");
        mutableMap.put("IY1", "i");
        mutableMap.put("IY2", "i");
        mutableMap.put("UW", "u");
        mutableMap.put("UW0", "u");
        mutableMap.put("UW1", "u");
        mutableMap.put("UW2", "u");
        mutableMap.put("EH", "e"); // modern versions use "e" instead of "ɛ"
        mutableMap.put("EH0", "e"); // ɛ
        mutableMap.put("EH1", "e"); // ɛ
        mutableMap.put("EH2", "e"); // ɛ
        mutableMap.put("IH", "ɪ");
        mutableMap.put("IH0", "ɪ");
        mutableMap.put("IH1", "ɪ");
        mutableMap.put("IH2", "ɪ");
        mutableMap.put("UH", "ʊ");
        mutableMap.put("UH0", "ʊ");
        mutableMap.put("UH1", "ʊ");
        mutableMap.put("UH2", "ʊ");
        mutableMap.put("AH", "ʌ");
        mutableMap.put("AH0", "ə");
        mutableMap.put("AH1", "ʌ");
        mutableMap.put("AH2", "ʌ");
        mutableMap.put("AE", "æ");
        mutableMap.put("AE0", "æ");
        mutableMap.put("AE1", "æ");
        mutableMap.put("AE2", "æ");
        mutableMap.put("AX", "ə");
        mutableMap.put("AX0", "ə");
        mutableMap.put("AX1", "ə");
        mutableMap.put("AX2", "ə");
                
                /*
		Vowels - Diphthongs
		Arpabet	IPA	Word Examples
		EY		eɪ	say (S EY1); eight (EY1 T)
		AY		aɪ	my (M AY1); why (W AY1); ride (R AY1 D)
		OW		oʊ	show (SH OW1); coat (K OW1 T)
		AW		aʊ	how (HH AW1); now (N AW1)
		OY		ɔɪ	boy (B OY1); toy (T OY1)
		*/
        mutableMap.put("EY", "eɪ");
        mutableMap.put("EY0", "eɪ");
        mutableMap.put("EY1", "eɪ");
        mutableMap.put("EY2", "eɪ");
        mutableMap.put("AY", "aɪ");
        mutableMap.put("AY0", "aɪ");
        mutableMap.put("AY1", "aɪ");
        mutableMap.put("AY2", "aɪ");
        mutableMap.put("OW", "oʊ");
        mutableMap.put("OW0", "oʊ");
        mutableMap.put("OW1", "oʊ");
        mutableMap.put("OW2", "oʊ");
        mutableMap.put("AW", "aʊ");
        mutableMap.put("AW0", "aʊ");
        mutableMap.put("AW1", "aʊ");
        mutableMap.put("AW2", "aʊ");
        mutableMap.put("OY", "ɔɪ");
        mutableMap.put("OY0", "ɔɪ");
        mutableMap.put("OY1", "ɔɪ");
        mutableMap.put("OY2", "ɔɪ");
		/*
		Consonants - Stops
		Arpabet	IPA	Word Examples
		P		p	pay (P EY1)
		B		b	buy (B AY1)
		T		t	take (T EY1 K)
		D		d	day (D EY1)
		K		k	key (K IY1)
		G		ɡ	go (G OW1)
		*/
        mutableMap.put("P", "p");
        mutableMap.put("B", "b");
        mutableMap.put("T", "t");
        mutableMap.put("D", "d");
        mutableMap.put("K", "k");
        mutableMap.put("G", "g");
		/*
		Consonants - Affricates
		Arpabet	IPA	Word Examples
		CH		tʃ	chair (CH EH1 R)
		JH		dʒ	just (JH AH1 S T); gym (JH IH1 M)
		*/
        mutableMap.put("CH", "tʃ");
        mutableMap.put("JH", "dʒ");

		/*
		Consonants - Fricatives
		Arpabet	IPA	Word Examples
		F		f	for (F AO1 R)
		V		v	very (V EH1 R IY0)
		TH		θ	thanks (TH AE1 NG K S); Thursday (TH ER1 Z D EY2)
		DH		ð	that (DH AE1 T); the (DH AH0); them (DH EH1 M)
		S		s	say (S EY1)
		Z		z	zoo (Z UW1)
		SH		ʃ	show (SH OW1)
		ZH		ʒ	measure (M EH1 ZH ER0); pleasure (P L EH1 ZH ER)
		HH		h	house (HH AW1 S)
		*/
        mutableMap.put("F", "f");
        mutableMap.put("V", "v");
        mutableMap.put("TH", "θ");
        mutableMap.put("DH", "ð");
        mutableMap.put("S", "s");
        mutableMap.put("Z", "z");
        mutableMap.put("SH", "ʃ");
        mutableMap.put("ZH", "ʒ");
        mutableMap.put("HH", "h");
		/*
		Consonants - Nasals
		Arpabet	IPA	Word Examples
		M		m	man (M AE1 N)
		N		n	no (N OW1)
		NG		ŋ	sing (S IH1 NG)
		*/
        mutableMap.put("M", "m");
        mutableMap.put("N", "n");
        mutableMap.put("NG", "ŋ");

		/*
		 Consonants - Liquids
		Arpabet	IPA		Word Examples
		L		ɫ OR l	late (L EY1 T)
		R		r OR ɹ	run (R AH1 N)
		*/
        mutableMap.put("L", "l");
        mutableMap.put("R", "r");
		/*
		 Vowels - R-colored vowels
		Arpabet			IPA	Word Examples
		ER				ɝ	her (HH ER0); bird (B ER1 D); hurt (HH ER1 T), nurse (N ER1 S)
		AXR				ɚ	father (F AA1 DH ER); coward (K AW1 ER D)
		The following R-colored vowels are contemplated above
		EH R			ɛr	air (EH1 R); where (W EH1 R); hair (HH EH1 R)
		UH R			ʊr	cure (K Y UH1 R); bureau (B Y UH1 R OW0), detour (D IH0 T UH1 R)
		AO R			ɔr	more (M AO1 R); bored (B AO1 R D); chord (K AO1 R D)
		AA R			ɑr	large (L AA1 R JH); hard (HH AA1 R D)
		IH R or IY R	ɪr	ear (IY1 R); near (N IH1 R)
		AW R			aʊr	This seems to be a rarely used r-controlled vowel. In some dialects flower (F L AW1 R; in other dialects F L AW1 ER0)
		*/
        mutableMap.put("ER", "ɜr");
        mutableMap.put("ER0", "ɜr");
        mutableMap.put("ER1", "ɜr");
        mutableMap.put("ER2", "ɜr");
        mutableMap.put("AXR", "ər");
        mutableMap.put("AXR0", "ər");
        mutableMap.put("AXR1", "ər");
        mutableMap.put("AXR2", "ər");
		/*
		Consonants - Semivowels
		Arpabet	IPA	Word Examples
		Y		j	yes (Y EH1 S)
		W		w	way (W EY1)
		*/
        mutableMap.put("W", "w");

        convertionMap = Collections.unmodifiableMap(mutableMap);
    }

    public static String convertToIpa(@NotNull String arpabetPronunciation){
        StringBuilder sb = new StringBuilder();
        String[] phonemes = arpabetPronunciation.split(" ");
        for (String phoneme : phonemes) {
            sb.append(convertionMap.get(phoneme));
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(convertToIpa("P R OW0 N AH2 N S IY0 EY1 SH AH0 N"));
        System.out.println(convertToIpa("P R AH0 N AH2 N S IY0 EY1 SH AH0 N"));
        System.out.println(convertToIpa("F AA1 DH ER0"));
    }
}
