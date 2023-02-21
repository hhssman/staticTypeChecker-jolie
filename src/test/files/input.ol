// from .types import C, ImportedInterface
// from .otherservice import EmbedInService, EmbedAsService, EmbedMeInterface1, EmbedMeInterface2

interface MyInterface {
	RequestResponse:
		helloReqRes( int )( string )
	OneWay:
		helloOneway( int )
}

// type T: void {
// 	a: A
// 	b: B
// }

// type A: int { x: int } | string

// type B: int | bool | double

type rec: int {
	x: rec
}

service MyService() {
	inputPort in {
		Location: "socket://localhost:8080"
		Protocol: http { format = "json" }
		Interfaces: MyInterface 
	}
	
	outputPort out {
		Location: "socket://localhost:8081"
		Protocol: http { format = "json" }
		Interfaces: MyInterface
	}

	main {
		a = 10
		// helloOneway@out(a)
		// helloReqRes@out(a)(out)

		// input = "hello"

		// [helloOneway(input)]{
		// 	input = 10
		// }

		[helloReqRes(input)(output.t.x){
			input += 10
			output.t.x = input
		}]{
			output = true
		}
	}
}