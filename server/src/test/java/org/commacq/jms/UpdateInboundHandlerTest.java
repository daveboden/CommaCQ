package org.commacq.jms;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.TextMessage;

import org.commacq.CsvDataSourceLayer;
import org.commacq.CsvUpdatableDataSource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateInboundHandlerTest {

    @Mock private CsvDataSourceLayer layer;
    @Mock private CsvUpdatableDataSource source;
    
    private UpdateInboundHandler handler;
    
    @Before
    public void setup() {
        handler = new UpdateInboundHandler(layer);
        when(layer.getCsvDataSource(anyString())).thenReturn(source);
    }
    
    @Test
    public void testTextMessageWithId() throws JMSException {
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getStringProperty("entityId")).thenReturn("testEntity");
        when(textMessage.getText()).thenReturn(
                "id" + "\n" +
                "abc"
        );
        when(source.getColumnNamesCsv()).thenReturn("id");
        
        handler.onMessage(textMessage);
        verify(source).getColumnNamesCsv();
        verify(source).startUpdateBlock("id");
        verify(source).updateUntrusted("abc");
        verify(source).finishUpdateBlock();
        verifyNoMoreInteractions(source);
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
        when(source.getColumnNamesCsv()).thenReturn("id");
        
        handler.onMessage(mapMessage);
        verify(source).getColumnNamesCsv();
        verify(source).startUpdateBlock("id");
        verify(source).updateUntrusted("abc");
        verify(source).updateUntrusted("def");
        verify(source).finishUpdateBlock();
        verifyNoMoreInteractions(source);
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
