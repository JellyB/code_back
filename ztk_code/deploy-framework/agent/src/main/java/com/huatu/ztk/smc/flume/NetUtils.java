package com.huatu.ztk.smc.flume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * This is a gets the local machine's domain name.
 */
public class NetUtils {
    public static final Logger LOG = LoggerFactory.getLogger(NetUtils.class);
    // this is recorded locally
    private static String localhost;

    // statically initialize localhost.
    static {
        if (localhost == null) {
            try {
                localhost = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (UnknownHostException e) {
                LOG.error("Unable to get canonical host name! " + e.getMessage(), e);
            }
        }
    }

    // this is really to avoid throwing an exception in the constructor.
    public static String localhost() {
        return localhost;
    }

    /**
     * This should only be used in test cases to force a particular host name.
     */
    public static void setLocalhost(String host) {
        localhost = host;
    }

    public static Pair<String, Integer> parseHostPortPair(String sock,
                                                          int portDefault) {
        String[] parts = sock.split(":");
        int port = portDefault;
        if (parts.length > 1) {
            port = Integer.parseInt(parts[1]);
        }
        return new Pair<String, Integer>(parts[0], port);
    }

    /**
     * Returns the index of the hostname/ip in the list that is the machine
     * running the process.
     *
     * @param hosts
     * @return the index of the array that is localhost, or -1 if localhost is not
     *         in the list.
     * @throws UnknownHostException
     * @throws SocketException
     */
    public static int findHostIndex(String[] hosts) throws UnknownHostException,
            SocketException {

        // if any addresses are loopbacks, return the index of the loopback addr.
        InetAddress[][] hostAddrsLst = new InetAddress[hosts.length][];
        for (int i = 0; i < hosts.length; i++) {
            // cache lists
            InetAddress[] hostAddrs = InetAddress.getAllByName(hosts[i]);
            hostAddrsLst[i] = InetAddress.getAllByName(hosts[i]);

            // check for loopbacks
            for (InetAddress hostAddr : hostAddrs) {
                if (hostAddr.isLoopbackAddress()) {
                    return i;
                }
            }
        }

        // for each nic
        Enumeration<NetworkInterface> nics = NetworkInterface
                .getNetworkInterfaces();
        while (nics.hasMoreElements()) {
            NetworkInterface nic = nics.nextElement();

            // for each ip address of that nic
            Enumeration<InetAddress> iaddrs = nic.getInetAddresses();
            while (iaddrs.hasMoreElements()) {
                InetAddress laddr = iaddrs.nextElement();

                // check each host to see if there is a match
                for (int i = 0; i < hosts.length; i++) {
                    List<InetAddress> maddrs = Arrays.asList(hostAddrsLst[i]);
                    if (maddrs.contains(laddr)) {
                        return i;
                    }
                }
            }
        }

        return -1; // didn't find it.
    }

}
