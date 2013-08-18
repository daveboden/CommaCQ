package org.commacq.jms;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.TextMessage;

import org.commacq.DataManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateInboundHandlerTest {

    @Mock private DataManager dataManager;
    
    private UpdateInboundHandler handler;
    
    @Before
    public void setup() {
        handler = new UpdateInboundHandler(dataManager);
    }
    
    @Test
    public void testTextMessageWithId() throws JMSException {
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getStringProperty("entityId")).thenReturn("testEntity");
        when(textMessage.getText()).thenReturn(
                "id" + "\n" +
                "abc"
        );
        handler.onMessage(textMessage);
        verify(dataManager).updateCsvCache(eq("testEntity"), listOf("abc"));
    }
    
    @Test
    public void testMapMessageWithId() throws JMSException {
        MapMessage mapMessage = mock(MapMessage.class);
        when(mapMessage.getMapNames()).thenReturn(Collections.enumeration(Collections.singleton("testEntity2")));
        when(mapMessage.getString("testEntity2")).thenReturn(
                "id" + "\n" +
                "abc" + "\n" + 
                "def" + "\n"
                );
        handler.onMessage(mapMessage);
        verify(dataManager).updateCsvCache(eq("testEntity2"), listOf("abc", "def"));
    }

    @Test
    public void testTextMessageWithGroup() throws JMSException {
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getStringProperty("entityId")).thenReturn("testEntity");
        when(textMessage.getText()).thenReturn(
                "currency" + "\n" +
                "abc"
        );
        handler.onMessage(textMessage);
        verify(dataManager).updateCsvCacheForGroup("testEntity", "currency", "abc");
    }
    
    
    
    
    private List<String> listOf(String... s) {
        return argThat(new ListOfStrings(s));
    }
    
    private class ListOfStrings extends ArgumentMatcher<List<String>> {
        
        private List<String> entry;
        
        public ListOfStrings(String... entry) {
            this.entry = Arrays.asList(entry);
        }
        @SuppressWarnings("unchecked")
        @Override
        public boolean matches(Object argument) {
            return ((List<String>)argument).equals(entry);
        }
    }

}
