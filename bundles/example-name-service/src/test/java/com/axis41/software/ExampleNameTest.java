package com.axis41.software;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axis41.software.impl.ExampleNameServiceImpl;

public class ExampleNameTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testGettingNamesArray() {
        ExampleNameServiceImpl ex = new ExampleNameServiceImpl();
        String name1 = "Batman";
        String name2 = "Robin";
        String[] sample = new String[]{name1, name2};
        ex.setNames(sample);
        logger.debug("Set names to {} in service.", Arrays.asList(sample) );
        
        String[] expected = {name1, name2};
        String[] actual = ex.getNames();
        logger.debug("Got names as {} from service.", Arrays.asList(actual) );

        assertTrue("Example names should not be empty.", actual != null && actual.length > 0);
        assertArrayEquals("Name should be lower case", expected, actual);
    }
}
