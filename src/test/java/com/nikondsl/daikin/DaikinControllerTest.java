package com.nikondsl.daikin;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DaikinControllerTest {
	private static  final String scanResponse = "ret=OK,type=aircon,reg=eu,dst=1,ver=3_3_1,pow=1,err=0,location=0,name=%d0%b3%d0%be%d1%81%d1%82%d0%b8%d0%bd%d0%b0%d1%8f,icon=2,method=polling,port=30050,id=mandarin,pw=purified,lpw_flag=0,adp_kind=2,pv=1,cpv=0,cpv_minor=00,led=1,en_setzone=1,mac=FABE20180111,adp_mode=run,en_hol=0,grp_name=,en_grp=0";
	
	@Mock
	private DaikinBase daikinBase;
	
	@Spy
	private DaikinController daikinController = spy(new DaikinController());
	
	@Test
	public void testScanResponseParse() throws IOException {
		when(daikinBase.getHost()).thenReturn("127.0.0.133");
		doReturn(Collections.singletonList(scanResponse)).when(daikinController).readIdentificationResponse(daikinBase);

        String[] nameAndAddressOfUnit = daikinController.checkApiExist(daikinBase);

        assertEquals("гостиная", nameAndAddressOfUnit[0]);
        assertEquals("127.0.0.133", nameAndAddressOfUnit[1]);
	}
	
}