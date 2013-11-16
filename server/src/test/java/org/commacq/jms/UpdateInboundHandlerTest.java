package org.commacq.jms;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.TextMessage;

import org.commacq.CsvUpdateBlockException;
import org.commacq.layer.UpdatableLayer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateInboundHandlerTest {

    @Mock private UpdatableLayer layer;
    
    private UpdateInboundHandler handler;
    
    @Before
    public void setup() {
        handler = new UpdateInboundHandler(layer);
    }
    
    @Test
    public void testTextMessageWithId() throws JMSException, CsvUpdateBlockException {
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getStringProperty("entityId")).thenReturn("testEntity");
        when(textMessage.getText()).thenReturn(
                "id" + "\n" +
                "abc"
        );
        when(layer.getColumnNamesCsv("testEntity")).thenReturn("id");
        
        handler.onMessage(textMessage);
        verify(layer).start(Collections.singleton("testEntity"));
        verify(layer).updateUntrusted("testEntity", "abc");
        verify(layer).finish();
        verifyNoMoreInteractions(layer);
    }
    
    @Test
    public void testMapMessageWithId() throws JMSException, CsvUpdateBlockException {
        MapMessage mapMessage = mock(MapMessage.class);
        when(mapMessage.getMapNames()).thenReturn(Collections.enumeration(Collections.singleton("testEntity2")));
        when(mapMessage.getString("testEntity2")).thenReturn(
                "id" + "\n" +
                "abc" + "\n" + 
                "def" + "\n"
                );
        when(layer.getColumnNamesCsv("testEntity2")).thenReturn("id");
        
        handler.onMessage(mapMessage);
        verify(layer).start(Collections.singleton("testEntity2"));
        verify(layer).updateUntrusted("testEntity2", "abc");
        verify(layer).updateUntrusted("testEntity2", "def");
        verify(layer).finish();
        verifyNoMoreInteractions(layer);
    }

    @Ignore //Can't handle groups yet
    @Test
    public void testTextMessageWithGroup() throws JMSException {
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getStringProperty("entityId")).thenReturn("testEntity");
        when(textMessage.getText()).thenReturn(
                "currency" + "\n" +
                "abc"
        );
        handler.onMessage(textMessage);
        //verify(dataManager).updateCsvCacheForGroup("testEntity", "currency", "abc");
    }

}
