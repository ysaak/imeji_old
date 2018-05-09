package ysaak.imeji.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CollectionUtilsTest {

	@Test
	public void testIsNotEmpty_nullCollection() {
		// Given

		// When
		boolean notEmpty = CollectionUtils.notEmpty(null);

		// Then
		Assert.assertFalse(notEmpty);
	}

	@Test
	public void testIsNotEmpty_emptyCollection() {
		// Given
		List<String> emptyList = Collections.emptyList();

		// When
		boolean notEmpty = CollectionUtils.notEmpty(emptyList);

		// Then
		Assert.assertFalse(notEmpty);
	}

	@Test
	public void testIsNotEmpty_notEmptyCollection() {
		// Given
		List<String> emptyList = Collections.singletonList("Item");

		// When
		boolean notEmpty = CollectionUtils.notEmpty(emptyList);

		// Then
		Assert.assertTrue(notEmpty);
	}
}
