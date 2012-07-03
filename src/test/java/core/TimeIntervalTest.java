package core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import core.Time.Unit;

/**
 * 
 * @author Patrick Maia
 *
 */
public class TimeIntervalTest {
	
	@Test(expected=IllegalArgumentException.class)
	public void testEndBeforeStart() {
		Time start = new Time(10, Unit.MILLISECONDS);
		Time end = Time.GENESIS;
		
		new TimeInterval(start, end);
	}

	@Test
	public void testOverlapCheck() {
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.MILLISECONDS);
		Time ten = new Time(10, Unit.MILLISECONDS);
		Time fifteen = new Time(15, Unit.MILLISECONDS);
		
		TimeInterval timeInterval1 = new TimeInterval(zero, five);
		TimeInterval timeInterval2 = new TimeInterval(ten, fifteen);
		TimeInterval timeInterval3 = new TimeInterval(five, fifteen);
		
		assertTrue(timeInterval1.overlaps(timeInterval1));
		
		assertFalse(timeInterval1.overlaps(timeInterval2));
		assertFalse(timeInterval2.overlaps(timeInterval1));
		
		assertTrue(timeInterval1.overlaps(timeInterval3));
		assertTrue(timeInterval3.overlaps(timeInterval1));
		
		assertTrue(timeInterval2.overlaps(timeInterval3));
		assertTrue(timeInterval3.overlaps(timeInterval2));
	}
	
	@Test
	public void testOverlappedIntervalMerge() {
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.MILLISECONDS);
		Time ten = new Time(10, Unit.MILLISECONDS);
		
		TimeInterval zeroFive = new TimeInterval(zero, five);
		TimeInterval zeroTen = new TimeInterval(zero, ten);
		
		TimeInterval merged = zeroFive.merge(zeroTen);
		assertEquals(zeroTen, merged);
		
		merged = zeroTen.merge(zeroFive);
		assertEquals(zeroTen, merged);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testNonOverlappedIntervalMerge() {
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.MILLISECONDS);
		Time ten = new Time(10, Unit.MILLISECONDS);
		Time twenty = new Time(20, Unit.MILLISECONDS);
		
		TimeInterval zeroFive = new TimeInterval(zero, five);
		TimeInterval tenTwenty = new TimeInterval(ten, twenty);
		
		zeroFive.merge(tenTwenty);
	}
}
