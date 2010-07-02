package edu.ncsu.csc.realsearch.apmeneel.devactivity.test;

import static edu.ncsu.csc.realsearch.apmeneel.devactivity.analysis.IterateOverDates.FORMAT;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import edu.ncsu.csc.realsearch.apmeneel.devactivity.analysis.IterateOverDates;
import edu.ncsu.csc.realsearch.apmeneel.devactivity.analysis.IterateOverDates.DateRange;
public class IterateOverDateTest {

	
	@Test
	public void oneLevel() throws Exception {
		List<DateRange> ranges = IterateOverDates.getRanges(FORMAT.parse("2010-01-20"), FORMAT.parse("2010-05-19"), 1);
		assertEquals(1, ranges.size());
		assertEquals("2010-01-20", FORMAT.format(ranges.get(0).getFrom()));
		assertEquals("2010-05-19", FORMAT.format(ranges.get(0).getTo()));
	}
	
	@Test
	public void twoLevels() throws Exception {
		List<DateRange> ranges = IterateOverDates.getRanges(FORMAT.parse("2010-01-20"), FORMAT.parse("2010-05-19"), 2);
		assertEquals(3, ranges.size());
		assertEquals("2010-01-20", FORMAT.format(ranges.get(0).getFrom()));
		assertEquals("2010-05-19", FORMAT.format(ranges.get(0).getTo()));
		
		assertEquals("2010-01-20", FORMAT.format(ranges.get(1).getFrom()));
		assertEquals("2010-03-20", FORMAT.format(ranges.get(1).getTo()));
		
		assertEquals("2010-03-20", FORMAT.format(ranges.get(2).getFrom()));
		assertEquals("2010-05-19", FORMAT.format(ranges.get(2).getTo()));
	
	}
	
	@Test
	public void fourLevels() throws Exception {
		List<DateRange> ranges = IterateOverDates.getRanges(FORMAT.parse("2010-01-20"), FORMAT.parse("2010-05-19"), 4);
		assertEquals(10, ranges.size());
	}
}