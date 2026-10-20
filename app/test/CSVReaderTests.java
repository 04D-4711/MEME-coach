
package test;

import org.junit.Test;
import persistentdata.formatted.CSVFormat;
import persistentdata.formatted.CSVReader;

import java.io.StringReader;

import static org.junit.Assert.*;

public class CSVReaderTests {

    private CSVReader makeReader(String content) {
        return new CSVReader(new CSVFormat(2), new StringReader(content));
    }

    @Test
    public void emptyFileHasNoNextRecord() {
        CSVReader reader = makeReader("");

        assertFalse(reader.hasNext());
    }

    @Test
    public void singleRecordCanBeRead() {
        CSVReader reader = makeReader("Alice,20\n");

        assertTrue(reader.hasNext());
        assertArrayEquals(new String[]{"Alice", "20"}, reader.getNext());
        assertFalse(reader.hasNext());
    }

    @Test
    public void multipleRecordsCanBeRead() {
        CSVReader reader = makeReader("Alice,20\nBob,21\n");

        assertTrue(reader.hasNext());
        assertArrayEquals(new String[]{"Alice", "20"}, reader.getNext());

        assertTrue(reader.hasNext());
        assertArrayEquals(new String[]{"Bob", "21"}, reader.getNext());

        assertFalse(reader.hasNext());
    }

    @Test
    public void finalRecordWithoutNewlineCanBeRead() {
        CSVReader reader = makeReader("Alice,20");

        assertTrue(reader.hasNext());
        assertArrayEquals(new String[]{"Alice", "20"}, reader.getNext());
        assertFalse(reader.hasNext());
    }

    @Test
    public void repeatedHasNextDoesNotSkipCharacters() {
        CSVReader reader = makeReader("Alice,20\n");

        assertTrue(reader.hasNext());
        assertTrue(reader.hasNext());
        assertTrue(reader.hasNext());

        assertArrayEquals(new String[]{"Alice", "20"}, reader.getNext());
        assertFalse(reader.hasNext());
    }
}

