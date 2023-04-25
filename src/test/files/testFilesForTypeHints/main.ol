type A: int


interface LolInterface{
	OneWay:
		lolOneWay(A)
}

inputPort PortName {
	Location: "socket://localhost:8080"
	Protocol: sodep
	Interfaces: LolInterface
}

main {
	x = 10
	x.y = "hey"
	y = true

	i = 0
	while(i < 3){
		x = "yo"
		y = 10
		i++
	}
	
}