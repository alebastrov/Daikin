package com.nikondsl.daikin.wireless;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class WirelessDaikinTest {
	private final static String controlResponse = "ret=OK,pow=1,mode=4,adv=,stemp=24.0,shum=0,dt1=25.0,dt2=M,dt3=25.0,dt4=24.0,dt5=24.0,dt7=25.0,dh1=AUTO,dh2=50,dh3=0,dh4=0,dh5=0,dh7=AUTO,dhh=50,b_mode=4,b_stemp=24.0,b_shum=0,alert=255,f_rate=A,f_dir=0,b_f_rate=A,b_f_dir=0,dfr1=5,dfr2=5,dfr3=5,dfr4=A,dfr5=A,dfr6=5,dfr7=5,dfrh=5,dfd1=0,dfd2=0,dfd3=0,dfd4=0,dfd5=0,dfd6=0,dfd7=0,dfdh=0";
	private final static String sensorResponse = "ret=OK,htemp=25.5,hhum=-,otemp=-1.0,err=0,cmpfreq=16";
	@Spy
	private WirelessDaikin wirelessDaikin = new WirelessDaikin("ip.address", 80);
	
	@Test
	public void testParseStatusResponses() throws IOException {
		doReturn(Collections.singletonList(controlResponse)).when(wirelessDaikin).readFromAdapter("/aircon/get_control_info");
		doReturn(Collections.singletonList(sensorResponse)).when(wirelessDaikin).readFromAdapter("/aircon/get_sensor_info");
		
		wirelessDaikin.readDaikinState();
		
		assertEquals("Wireless Daikin unit [ \n" +
				"  Host: ip.address\n" +
				"  Power: ON\n" +
				"  Mode: Heat\n" +
				"  Fan: Auto\n" +
				"  Fan direction: Off\n" +
				"  Target humidity: 0\n" +
				"  Target temperature: 24.0\n" +
				"  Inside temperature: 25.5\n" +
				"  Outside temperature: -1.0\n" +
				"]", wirelessDaikin.toString());
		assertEquals(80, wirelessDaikin.getPort());
	}
}