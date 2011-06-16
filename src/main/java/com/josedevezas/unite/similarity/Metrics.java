package com.josedevezas.unite.similarity;

import com.josedevezas.unite.util.Logging;

import org.apache.log4j.Logger;

public class Metrics
{
	private static final Logger log;

	static
	{
		log = Logging.getLogger( Metrics.class.getName() );
	}

	/**
	 * Tanimoto vector similarity.
	 *
	 * Adapted from Filipe Coelho's C# implementation.
	 *
	 * @param table1 First descriptor vector.
	 * @param table2 Second descriptor vector.
	 * @return The similarity value.
	 */
	public static Double tanimotoClassifier( Double[] table1, Double[] table2 )
	{
		Double result = 0D;
		Double temp1 = 0D;
		Double temp2 = 0D;

		Double tempCount1 = 0D, tempCount2 = 0D, tempCount3 = 0D;

		for ( int i = 0 ; i < table1.length ; i++ )
		{
			temp1 += table1[i];
			temp2 += table2[i];
		}

		if ( temp1 == 0 || temp2 == 0 ) result = 100D;
		if ( temp1 == 0 && temp2 == 0 ) result = 0D;

		if ( temp1 > 0 && temp2 > 0 )
		{
			for ( int i = 0 ; i < table1.length ; i++ )
			{
				tempCount1 += ( table1[i] / temp1 ) * ( table2[i] / temp2 );
				tempCount2 += ( table2[i] / temp2 ) * ( table2[i] / temp2 );
				tempCount3 += ( table1[i] / temp1 ) * ( table1[i] / temp1 );
			}

			result = ( 100 - 100 * ( tempCount1 / (tempCount2 + tempCount3 - tempCount1 ) ) );
		}

		return result;
	}

	public static Double euclideanDistance( Double[] table1, Double[] table2 )
	{
		if ( table1.length != table2.length )
		{
			log.warn( "Different vector sizes, skipping" );
			return 0D;
		}

		Double sum = 0D;

		for ( int i = 0 ; i < table1.length ; i++ )
			sum += Math.pow( table1[ i ] - table2[ i ], 2 );

		return sum;
	}
}
