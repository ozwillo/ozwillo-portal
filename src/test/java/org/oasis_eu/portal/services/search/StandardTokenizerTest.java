package org.oasis_eu.portal.services.search;

import org.junit.jupiter.api.Test;
import org.oasis_eu.portal.model.search.StandardTokenizer;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StandardTokenizerTest {
	StandardTokenizer tokenizer = new StandardTokenizer();

	@Test
	public void tokenizeLatin() {

		assertEquals(Arrays.asList("saint", "genis", "les", "ollieres"), tokenizer.tokenize("Saint - Genis-Les-Ollières"));
		assertEquals(Arrays.asList("arvizturo", "tukorfurogep"), tokenizer.tokenize("árvíztűrő tükörfúrógép"));

	}

	@Test
	public void tokenizeCyrillic() {
		List<String> list = tokenizer.tokenize("Айтос");
		assertEquals(1, list.size());
		assertEquals("аитос", list.get(0));
	}

	@Test
	public void tokenizeTurkish() {
		assertEquals(Arrays.asList("ı", "g", "g", "s"), tokenizer.tokenize("ı ğ Ğ Ş"));
	}

	@Test
	public void tokenizeWeirdAccentedLetters() {
		// ŵ is Welsh, ç is French/Turkish, ĉ is Esperanto
		// icelandic ð and þ remain ð and þ, that's fair, but they're correctly made lower case
		assertEquals(Arrays.asList("w", "c", "c", "ð", "þ"), tokenizer.tokenize("ŵ Ç ĉ Ð Þ"));

		// bit of Irish
		assertEquals(Arrays.asList("slante"), tokenizer.tokenize("Slánte"));
	}

	/*
	Test that Tok not only removes punctuation marks like !,- but it also doesn't create bogus empty tokens
	 */
	@Test
	public void punctuation() {
		assertEquals(Arrays.asList("hardi", "souquez", "ferme", "moussaillons", "il", "y", "aura", "surement", "du", "rhum"),
				tokenizer.tokenize("Hardi ! Souquez ferme, moussaillons -- il y aura, sûrement, du rhum !"));
	}
}