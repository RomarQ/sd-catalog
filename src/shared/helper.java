package shared;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class helper {

    /**
     * Gets the active interface address
     *
     * Needed for some linux distros since some sometimes InetAddress
     * returns localhost address instead of interface address
     *
     * @return
     * @throws SocketException
     */
    public static String getIp() throws SocketException {
        Enumeration en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = (NetworkInterface) en.nextElement();
            for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements();) {
                InetAddress addr = (InetAddress) en2.nextElement();
                if (!addr.isLoopbackAddress())
                    if (addr instanceof Inet4Address)
                        return addr.getHostAddress();
            }
        }
        return null;
    }

}
