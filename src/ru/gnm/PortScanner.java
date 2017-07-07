package ru.gnm;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ru.gnm.Main.*;

/**
 * Created by Gordeyev Nikita on 18.04.17.
 * mailto: gordeevnm@gmail.com
 */
public class PortScanner {
	private int ipLeft;
	private int ipRight;
	private int portLeft;
	private int portRight;
	private int timeout;
	private ConcurrentHashMap<String, StringBuffer> opened = new ConcurrentHashMap<>();

	public PortScanner(Map<String, String> params) throws Exception {
		setAddresses(params.get(PARAM_ADDRESSES_NAME));
		setPorts(params.get(PARAM_PORTS_NAME));
		setTimeout(params.get(PARAM_TIMEOUT_NAME));
	}

	public static int toInt(byte[] bytes) {
		int ret = 0;
		for (int i = 0; i < 4; i++) {
			ret <<= 8;
			ret |= (int) bytes[i] & 0xFF;
		}
		return ret;
	}

	public void scan() throws UnknownHostException, InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(8);
		for (int current = ipLeft; current <= ipRight; current++) {
			int[] currBytes = {
					(current >> 24) & 0xff,
					(current >> 16) & 0xff,
					(current >> 8) & 0xff,
					(current) & 0xff
			};
			String host = currBytes[0] + "." + currBytes[1] + "." + currBytes[2] + "." + currBytes[3];

			executorService.submit(() -> {
				try {
					if (InetAddress.getByName(host).isReachable(timeout)) {
						for (int currPort = portLeft; currPort < portRight; currPort++) {
							Socket socket = null;
							try {
								socket = new Socket();
								SocketAddress address = new InetSocketAddress(host, currPort);
								socket.connect(address, timeout);
								opened.computeIfAbsent(host, s -> new StringBuffer()).append("\t" + currPort + "\t opened\n");
							} catch (SocketTimeoutException e) {
								opened.computeIfAbsent(host, s -> new StringBuffer()).append("\t" + currPort + "\t timeout\n");
							} catch (ConnectException ignored) {
							} finally {
								if (socket != null)
									socket.close();
							}

						}
					}
				} catch (IOException ignored) {

				}
			});
		}

		executorService.shutdown();
		executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

		for (ConcurrentHashMap.Entry entry : opened.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue());
		}
	}

	public void setAddresses(String s) throws UnknownHostException {
		String[] ips = s.split("-");
		if (ips.length != 2)
			throw new IllegalArgumentException("Invalid ip address range");
		InetAddress ip1 = InetAddress.getByName(ips[0]);
		InetAddress ip2 = InetAddress.getByName(ips[1]);
		this.ipLeft = toInt(ip1.getAddress());
		this.ipRight = toInt(ip2.getAddress());
		if (ipLeft > ipRight)
			throw new IllegalArgumentException("Invalid address range");
	}

	public void setPorts(String ports) throws Exception {
		if (!ports.matches("\\d+-\\d+"))
			throw new IllegalArgumentException("Invalid ports range");
		String[] port = ports.split("-");
		portLeft = Integer.parseInt(port[0]);
		portRight = Integer.parseInt(port[1]);
		if (portLeft > portRight || portLeft < 1 || portRight > 65535)
			throw new Exception("Invalid ports range");
	}

	public void setTimeout(String s) throws Exception {
		if (!s.matches("\\d+"))
			throw new Exception("Invalid timeout");
		this.timeout = Integer.parseInt(s);
	}
}
