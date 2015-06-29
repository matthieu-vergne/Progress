package fr.vergne.progress.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class ProgressUtilTest {

	@Test
	public void testIntegerPercentageBetween0And100() {
		for (int i = 0; i <= 1000; i++) {
			int percent = ProgressUtil.computeIntegerPercentage(i, 1000);
			assertTrue("Not in [0;100]: " + percent, percent >= 0
					&& percent <= 100);
		}
	}

	@Test
	public void testIntegerPercentageProperlyFloored() {
		for (int i = 0; i < 10; i++) {
			assertEquals(0, ProgressUtil.computeIntegerPercentage(i, 1000));
		}
		assertEquals(1, ProgressUtil.computeIntegerPercentage(10, 1000));

		for (int i = 50; i < 60; i++) {
			assertEquals(5, ProgressUtil.computeIntegerPercentage(i, 1000));
		}
		assertEquals(6, ProgressUtil.computeIntegerPercentage(60, 1000));

		for (int i = 990; i < 1000; i++) {
			assertEquals(99, ProgressUtil.computeIntegerPercentage(i, 1000));
		}
		assertEquals(100, ProgressUtil.computeIntegerPercentage(1000, 1000));
	}

	@Test
	public void testToStringOnSpecificCasesPreviouslyWrong() {
		assertEquals("1644/1800 (91%)", ProgressUtil.toString(new ManualProgress<Integer>(1644, 1800)));
	}

}
