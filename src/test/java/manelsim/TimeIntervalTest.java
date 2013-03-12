package manelsim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import manelsim.Time.Unit;

import org.junit.Test;


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
	
	@Test
	public void testSubsetIntervalsIntersection() {
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.SECONDS);
		Time six = new Time(6, Unit.SECONDS);
		Time ten = new Time(10, Unit.SECONDS);
		
		TimeInterval intervalOne = new TimeInterval(zero, ten);
		TimeInterval intervalTwo = new TimeInterval(five, six);
		
		assertEquals(intervalOne.intersection(intervalTwo), intervalTwo.intersection(intervalOne));
		assertEquals(intervalTwo, intervalOne.intersection(intervalTwo));
	}
	
	@Test
	public void testOverlappedIntervalsIntersection() {
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.SECONDS);
		Time six = new Time(6, Unit.SECONDS);
		Time ten = new Time(10, Unit.SECONDS);
		
		TimeInterval intervalOne = new TimeInterval(zero, six);
		TimeInterval intervalTwo = new TimeInterval(five, ten);
		
		assertEquals(intervalOne.intersection(intervalTwo), intervalTwo.intersection(intervalOne));
		TimeInterval expected = new TimeInterval(five, six);
		assertEquals(expected, intervalOne.intersection(intervalTwo));
	}
	
	@Test
	public void testEmptyIntersection() {
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.SECONDS);
		Time six = new Time(6, Unit.SECONDS);
		Time ten = new Time(10, Unit.SECONDS);
		
		TimeInterval intervalOne = new TimeInterval(zero, five);
		TimeInterval intervalTwo = new TimeInterval(six, ten);
		
		assertEquals(intervalOne.intersection(intervalTwo), intervalTwo.intersection(intervalOne));
		assertNull(intervalOne.intersection(intervalTwo));
	}
	
	@Test
	public void testEqualsIntervalsIntersection() {
		Time zero = Time.GENESIS;
		Time ten = new Time(10, Unit.SECONDS);
		
		TimeInterval intervalOne = new TimeInterval(zero, ten);
		TimeInterval intervalTwo = new TimeInterval(zero, ten);
		
		assertEquals(intervalOne.intersection(intervalTwo), intervalTwo.intersection(intervalOne));
		assertEquals(new TimeInterval(zero, ten), intervalOne.intersection(intervalTwo));
	}
	
	@Test
	public void testSameBeginIntervalsIntersection() {
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.SECONDS);
		Time six = new Time(6, Unit.SECONDS);
		
		TimeInterval intervalOne = new TimeInterval(zero, five);
		TimeInterval intervalTwo = new TimeInterval(zero, six);
		
		assertEquals(intervalOne.intersection(intervalTwo), intervalTwo.intersection(intervalOne));
		TimeInterval expected = new TimeInterval(zero, five);
		assertEquals(expected, intervalOne.intersection(intervalTwo));
	}
	
	@Test
	public void testSameEndIntervalsIntersection() {
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.SECONDS);
		Time six = new Time(6, Unit.SECONDS);
		
		TimeInterval intervalOne = new TimeInterval(zero, six);
		TimeInterval intervalTwo = new TimeInterval(five, six);
		
		assertEquals(intervalOne.intersection(intervalTwo), intervalTwo.intersection(intervalOne));
		TimeInterval expected = new TimeInterval(five, six);
		assertEquals(expected, intervalOne.intersection(intervalTwo));
	}
	
	@Test
	public void testSubsetIntervalsDiff() {
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.SECONDS);
		Time six = new Time(6, Unit.SECONDS);
		Time ten = new Time(10, Unit.SECONDS);
		
		TimeInterval intervalOne = new TimeInterval(zero, ten);
		TimeInterval intervalTwo = new TimeInterval(five, six);
		
		TimeInterval []  diff = intervalOne.diff(intervalTwo);
		assertEquals(new TimeInterval(zero, five), diff[0]);
		assertEquals(new TimeInterval(six, ten), diff[1]);
		
		diff = intervalTwo.diff(intervalOne);
		assertNull(diff[0]);
		assertNull(diff[1]);
	}
	
	@Test
	public void testOverlappedIntervalsDiff() {
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.SECONDS);
		Time six = new Time(6, Unit.SECONDS);
		Time ten = new Time(10, Unit.SECONDS);
		
		TimeInterval intervalOne = new TimeInterval(zero, six);
		TimeInterval intervalTwo = new TimeInterval(five, ten);
		
		TimeInterval [] diff = intervalOne.diff(intervalTwo);
		assertEquals(new TimeInterval(zero, five), diff[0]);
		assertNull(diff[1]);
		
		diff = intervalTwo.diff(intervalOne);
		assertEquals(new TimeInterval(six, ten), diff[0]);
		assertNull(diff[1]);
	}
	
	@Test
	public void testNonOverlappedIntervalsDiff() {
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.SECONDS);
		Time six = new Time(6, Unit.SECONDS);
		Time ten = new Time(10, Unit.SECONDS);
		
		TimeInterval intervalOne = new TimeInterval(zero, five);
		TimeInterval intervalTwo = new TimeInterval(six, ten);
		
		TimeInterval [] diff = intervalOne.diff(intervalTwo);
		
		assertEquals(intervalOne, diff[0]);
		assertNull(diff[1]);
		
		diff = intervalTwo.diff(intervalOne);
		assertEquals(intervalTwo, diff[0]);
		assertNull(diff[1]);
	}
	
	@Test
	public void testEqualsIntervalsDiff() {
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.SECONDS);
		
		TimeInterval intervalOne = new TimeInterval(zero, five);
		TimeInterval intervalTwo = new TimeInterval(zero, five);
		
		TimeInterval [] diff = intervalOne.diff(intervalTwo);
		
		assertNull(diff[0]);
		assertNull(diff[1]);
	}
	
	@Test
	public void testSameBeginIntervalsDiff() {
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.SECONDS);
		Time six = new Time(6, Unit.SECONDS);
		
		TimeInterval intervalOne = new TimeInterval(zero, five);
		TimeInterval intervalTwo = new TimeInterval(zero, six);
		
		TimeInterval [] diff = intervalOne.diff(intervalTwo);
		assertNull(diff[0]);
		assertNull(diff[1]);
		
		diff = intervalTwo.diff(intervalOne);
		assertEquals(new TimeInterval(five, six), diff[0]);
		assertNull(diff[1]);
	}
	
	@Test
	public void testSameEndIntervalsDiff() {
		Time zero = Time.GENESIS;
		Time five = new Time(5, Unit.SECONDS);
		Time six = new Time(6, Unit.SECONDS);
		
		TimeInterval intervalOne = new TimeInterval(zero, six);
		TimeInterval intervalTwo = new TimeInterval(five, six);
		
		TimeInterval [] diff = intervalOne.diff(intervalTwo);
		assertEquals(new TimeInterval(zero, five), diff[0]);
		assertNull(diff[1]);
		
		diff = intervalTwo.diff(intervalOne);
		assertNull(diff[0]);
		assertNull(diff[1]);
	}
	
}
