interface MessageInterface{
    OneWay:
        sendNumberOneWay(int)
}

service Main() {
    outputPort messageSender{
        Interfaces: MessageInterface
    }

    main {
        message = 42
        if(message < 100) {
            message = "To big"
        }
        sendNumberOneWay@messageSender(message)
    }
}