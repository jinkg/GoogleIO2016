package com.yalin.googleio2016.messaging;

/**
 * YaLin
 * 2016/11/24.
 * <p>
 * Implements this for messaging service. A messaging service is a service enabling a backend server
 * to send messages to the app, and the device needs to be registered with it. Additionally, the
 * device needs to be registered with the backend server that is actually responsible for deciding
 * when/which message to send.
 */

public interface MessagingRegistration {
    /**
     * Implements registering the device with the messaging service that will take care of
     * delivering the messages send by the backend server, if required, and registering the device
     * with the messaging service that will take care of delivering the messages send by the backend
     * server, if required.
     */
    void registerDevice();

    /**
     * Implements un-registering {@code accountName} with the device.
     */
    void unregisterDevice(String accountName);

    /**
     * Implements to canceling any operation in progress.
     */
    void destory();
}
