interface MessageInterface{
    RequestResponse:
        sendNumber(int)(void)
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
        sendNumber@messageSender(message)()
    }
}