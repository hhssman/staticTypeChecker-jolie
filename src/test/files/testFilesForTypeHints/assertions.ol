interface AssertionsInterface{
	OneWay:
		assert( bool )
}

service Assertions{
	inputPort in {
		Location: local
		Protocol: sodep
		Interfaces: AssertionsInterface
	}

	main{
		[assert(inBool)]{
			nullProcess	
		}
	}
}