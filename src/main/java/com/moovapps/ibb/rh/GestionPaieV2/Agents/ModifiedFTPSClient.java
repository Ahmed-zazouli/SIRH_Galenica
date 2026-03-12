package com.moovapps.ibb.rh.GestionPaieV2.Agents;

//import org.apache.commons.net.ftp.FTPSClient;

public class ModifiedFTPSClient /*extends FTPSClient */{

//	public ModifiedFTPSClient() {
//		super("TLS", false);
//	}
//
//	public ModifiedFTPSClient(boolean isImplicit) {
//		super("TLS", isImplicit);
//	}
//
//	@Override
//	protected void _prepareDataSocket_(final Socket socket) throws IOException {
//		if (socket instanceof SSLSocket) {
//			final SSLSession session = ((SSLSocket) _socket_).getSession();
//			if (session.isValid()) {
//				final SSLSessionContext context = session.getSessionContext();
//				try {
//					final Field sessionHostPortCache = context.getClass()
//							.getDeclaredField("sessionHostPortCache");
//					sessionHostPortCache.setAccessible(true);
//					final Object cache = sessionHostPortCache.get(context);
//					final java.lang.reflect.Method method = cache.getClass()
//							.getDeclaredMethod("put", Object.class,
//									Object.class);
//					method.setAccessible(true);
//					method.invoke(
//							cache,
//							String.format("%s:%s",
//									socket.getInetAddress().getHostName(),
//									String.valueOf(socket.getPort()))
//									.toLowerCase(Locale.ROOT), session);
//					method.invoke(
//							cache,
//							String.format("%s:%s",
//									socket.getInetAddress().getHostAddress(),
//									String.valueOf(socket.getPort()))
//									.toLowerCase(Locale.ROOT), session);
//				} catch (NoSuchFieldException e) {
//					throw new IOException(e);
//				} catch (Exception e) {
//					throw new IOException(e);
//				}
//			} else {
//				throw new IOException("Invalid SSL Session");
//			}
//		}
//	}
}