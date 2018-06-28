/*
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.bolt.v1.messaging;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import org.neo4j.bolt.runtime.BoltConnection;
import org.neo4j.bolt.runtime.BoltResult;
import org.neo4j.bolt.v1.runtime.spi.ImmutableRecord;
import org.neo4j.cypher.result.QueryResult.Record;
import org.neo4j.logging.NullLog;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.neo4j.values.storable.Values.values;

class ResultHandlerTest
{
    @Test
    void shouldPullTheResult() throws Exception
    {
        BoltResponseMessageHandler<IOException> responseMessageHandler = mock( BoltResponseMessageHandler.class );
        ResultHandler handler = new ResultHandler( responseMessageHandler, mock( BoltConnection.class ), NullLog.getInstance() );

        ImmutableRecord record1 = new ImmutableRecord( values( "a", "b", "c" ) );
        ImmutableRecord record2 = new ImmutableRecord( values( "1", "2", "3" ) );
        BoltResult result = new TestBoltResult( record1, record2 );

        handler.onRecords( result, true );
        handler.onFinish();

        verify( responseMessageHandler ).onRecord( record1 );
        verify( responseMessageHandler ).onRecord( record2 );
        verify( responseMessageHandler ).onSuccess( any() );
    }

    @Test
    void shouldDiscardTheResult() throws Exception
    {
        BoltResponseMessageHandler<IOException> responseMessageHandler = mock( BoltResponseMessageHandler.class );
        ResultHandler handler = new ResultHandler( responseMessageHandler, mock( BoltConnection.class ), NullLog.getInstance() );

        ImmutableRecord record1 = new ImmutableRecord( values( "a", "b", "c" ) );
        ImmutableRecord record2 = new ImmutableRecord( values( "1", "2", "3" ) );
        BoltResult result = new TestBoltResult( record1, record2 );

        handler.onRecords( result, false );
        handler.onFinish();

        verify( responseMessageHandler, never() ).onRecord( any() );
        verify( responseMessageHandler ).onSuccess( any() );
    }

    private static class TestBoltResult implements BoltResult
    {
        private final Record[] records;

        private TestBoltResult( Record... records )
        {
            this.records = records;
        }

        @Override
        public String[] fieldNames()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void accept( Visitor visitor ) throws Exception
        {
            for ( Record record: records )
            {
                visitor.visit( record );
            }
        }

        @Override
        public void close()
        {
        }
    }
}
