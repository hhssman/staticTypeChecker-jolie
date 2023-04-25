interface ImportedInterface1{
	RequestResponse:
	OneWay:
}

interface ImportedInterface2 {
	RequestResponse:
	OneWay:
}

service ImportService1(a: int){
	inputPort InputPort1 {
		Location: "socket://localhost:8082"
		Protocol: sodep
		Interfaces: ImportedInterface1
	}
}

service ImportService2(){
	inputPort InputPort2 {
		Location: "socket://localhost:8083"
		Protocol: http
		Interfaces: ImportedInterface2
	}
}