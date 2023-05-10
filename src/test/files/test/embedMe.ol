interface EmbedMeInterface{

}

interface EmbedMeInterface2{

}

service EmbedMe(){
	inputPort EmbedMeInputPort {
		Location: "local"
		Protocol: sodep
		Interfaces: EmbedMeInterface
	}

	inputPort EmbedMeInputPort2 {
		Location: "locasl"
		Protocol: sodep
		Interfaces: EmbedMeInterface2
	}

	main{
		 a = 0
	}
}