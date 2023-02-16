// from .types import C, ImportedInterface
// from .otherservice import EmbedInService, EmbedAsService, EmbedMeInterface1, EmbedMeInterface2

interface MyInterface {
	RequestResponse:
		helloReqRes( B )( string )
	OneWay:
		helloOneway( A )
}

type T: void {
	a: A
	b: B
}

type A: int { x: int } | string

type B: int | bool | double

service MyService() {
	
	outputPort out {
		Location: "socket://localhost:8081"
		Protocol: http { format = "json" }
		Interfaces: MyInterface
	}

	main {
		a = 10
		helloOneway@out(a)
	}
}