package com.serverless.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

@DynamoDBTable(tableName = "PLACEHOLDER_USERS_TABLE_NAME")
public class User {

    // get the table name from env. var. set in serverless.yml
    private static final String USERS_TABLE_NAME = System.getenv("USERS_TABLE_NAME");

    private static DynamoDBAdapter db_adapter;
    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;

    private Logger logger = Logger.getLogger(this.getClass());

    private String id;
    private String name;
    private Float price;

    @DynamoDBHashKey(attributeName = "id")
    @DynamoDBAutoGeneratedKey
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBRangeKey(attributeName = "name")
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBAttribute(attributeName = "price")
    public Float getPrice() {
        return this.price;
    }
    public void setPrice(Float price) {
        this.price = price;
    }

    public User() {
        // build the mapper config
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(USERS_TABLE_NAME))
            .build();
        // get the db adapter
        this.db_adapter = DynamoDBAdapter.getInstance();
        this.client = this.db_adapter.getDbClient();
        // create the mapper with config
        this.mapper = this.db_adapter.createDbMapper(mapperConfig);
    }

    public String toString() {
        return String.format("User [id=%s, name=%s, price=$%f]", this.id, this.name, this.price);
    }

    // methods
    public Boolean ifTableExists() {
        return this.client.describeTable(USERS_TABLE_NAME).getTable().getTableStatus().equals("ACTIVE");
    }

    public List<User> list() throws IOException {
      DynamoDBScanExpression scanExp = new DynamoDBScanExpression();
      List<User> results = this.mapper.scan(User.class, scanExp);
      for (User p : results) {
        logger.info("USERS - list(): " + p.toString());
      }
      return results;
    }

    public User get(String id) throws IOException {
        User User = null;

        HashMap<String, AttributeValue> av = new HashMap<String, AttributeValue>();
        av.put(":v1", new AttributeValue().withS(id));

        DynamoDBQueryExpression<User> queryExp = new DynamoDBQueryExpression<User>()
            .withKeyConditionExpression("id = :v1")
            .withExpressionAttributeValues(av);

        PaginatedQueryList<User> result = this.mapper.query(User.class, queryExp);
        if (result.size() > 0) {
          User = result.get(0);
          logger.info("USERS - get(): User - " + User.toString());
        } else {
          logger.info("USERS - get(): User - Not Found.");
        }
        return User;
    }

    public void save(User User) throws IOException {
        logger.info("USERS - save(): " + User.toString());
        this.mapper.save(User);
    }

    public Boolean delete(String id) throws IOException {
        User User = null;

        // get User if exists
        User = get(id);
        if (User != null) {
          logger.info("USERS - delete(): " + User.toString());
          this.mapper.delete(User);
        } else {
          logger.info("USERS - delete(): User - does not exist.");
          return false;
        }
        return true;
    }

}