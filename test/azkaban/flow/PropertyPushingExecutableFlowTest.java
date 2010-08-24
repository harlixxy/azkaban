package azkaban.flow;

import azkaban.common.utils.Props;
import org.easymock.Capture;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class PropertyPushingExecutableFlowTest
{
    ExecutableFlow childFlow;
    FlowCallback callback;

    PropertyPushingExecutableFlow flow;

    @Before
    public void setUp()
    {
        childFlow = EasyMock.createMock(ExecutableFlow.class);
        callback = EasyMock.createMock(FlowCallback.class);
    }

    @After
    public void tearDown()
    {
    }

    @Test
    public void testSanity() throws Throwable
    {
        Props props = new Props();
        props.put("billy", "blank");

        EasyMock.expect(childFlow.getStatus()).andReturn(Status.READY).times(3);
        EasyMock.expect(childFlow.getStartTime()).andReturn(null).once();
        EasyMock.replay(childFlow);

        flow = new PropertyPushingExecutableFlow("blah", "test", props, childFlow);

        EasyMock.verify(childFlow);
        EasyMock.reset(childFlow);

        Capture<Props> propsCap = new Capture<Props>();
        Capture<FlowCallback> callbackCap = new Capture<FlowCallback>();

        childFlow.execute(EasyMock.capture(propsCap), EasyMock.capture(callbackCap));
        EasyMock.expectLastCall();
        EasyMock.replay(childFlow);

        Props otherProps = new Props();
        otherProps.put("sally", "jesse");
        otherProps.put("billy", "bob");  // should be overriden

        flow.execute(otherProps, callback);

        EasyMock.verify(childFlow);
        EasyMock.reset(childFlow);

        callback.completed(Status.SUCCEEDED);
        EasyMock.expectLastCall();

        EasyMock.expect(childFlow.getStatus()).andReturn(Status.SUCCEEDED);

        EasyMock.replay(childFlow, callback);

        callbackCap.getValue().completed(Status.SUCCEEDED);

        EasyMock.verify(callback, childFlow);

        Props cappedProps = propsCap.getValue();
        Assert.assertEquals(2, cappedProps.size());
        Assert.assertEquals("blank", cappedProps.get("billy"));
        Assert.assertEquals("jesse", cappedProps.get("sally"));

    }

}
