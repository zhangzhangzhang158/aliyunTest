package org.aming.web.qq.repository.jdbc.impl;

import com.google.common.collect.Maps;
import org.aming.web.qq.domain.Message;
import org.aming.web.qq.domain.Page;
import org.aming.web.qq.domain.TimeInterval;
import org.aming.web.qq.domain.User;
import org.aming.web.qq.exceptions.WebQQDaoException;
import org.aming.web.qq.repository.jdbc.MessageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * @author daming
 * @version 2017/10/5.
 */
@Repository
public class MessageDaoImpl implements MessageDao {

    private JdbcTemplate jdbcTemplate;

    private static final String SQL_SAVE_MESSAGE = " INSERT INTO message (id,sendUserId,receiveUserId,content,sendDate,sendDeleteFlag,receiveDeleteFlag,create_id,creation_date,update_id,update_date) SELECT ?,A.id,B.id,?,?,?,?,'system',NOW(),'system',NOW() FROM `user` A  JOIN USER B ON  B.username = ? WHERE A.username = ? ";
    public int saveMessage(Message message) throws WebQQDaoException {
        try{
            Object[]  args = new Object[] {
                    message.getId(),
                    message.getContent(),
                    new java.sql.Timestamp(
                            message.getSendDate() != null ? message.getSendDate().getTime() : System.currentTimeMillis()),
                    message.getSendDeleteFlag(),
                    message.getReceiveDeleteFlag(),
                    message.getReceiveUser().getUsername(),
                    message.getSendUser().getUsername()
            };
            int[]  argTypes = new int[] {
                    Types.VARBINARY,
                    Types.VARBINARY,
                    Types.TIMESTAMP,
                    Types.INTEGER,
                    Types.INTEGER,
                    Types.VARBINARY,
                    Types.VARBINARY
            };
            return jdbcTemplate.update(
                    SQL_SAVE_MESSAGE,
                    args,
                    argTypes
            );
        } catch (Exception ex) {
            throw new WebQQDaoException(SQL_SAVE_MESSAGE,ex);
        }

    }

    private static final String SQL_GET_MESSAGE_PAGE = "SELECT id,sendUserId,receiveUserId,content,sendDate,sendDeleteFlag,receiveDeleteFlag FROM message A  WHERE (A.sendUserId = ? AND A.receiveUserId = ?) OR (A.sendUserId = ? AND A.receiveUserId = ?) ORDER BY A.sendDate DESC LIMIT ?,?";
    public List<Message> getMessage(User sendUser, User receiveUser, Page page) throws WebQQDaoException {
        try {
            Map<String,User> map = Maps.newHashMap();
            map.put(sendUser.getId(),sendUser);
            map.put(receiveUser.getId(),receiveUser);

            Object[] args = new Object[]{
                    sendUser.getId(),
                    receiveUser.getId(),
                    receiveUser.getId(),
                    sendUser.getId(),
                    page.getCurrentPage(),
                    page.getPageSize()
            };
            int[] argTypes = new int[]{
                    Types.VARCHAR,
                    Types.VARCHAR,
                    Types.VARCHAR,
                    Types.VARCHAR,
                    Types.INTEGER,
                    Types.INTEGER
            };
            return jdbcTemplate.query(
                    SQL_GET_MESSAGE_PAGE,
                    args,
                    argTypes,
                    (rs, i) -> getMessageByResultSet(rs)
                            .setSendUser(map.get(rs.getString("sendUserId")))
                            .setReceiveUser(map.get(rs.getString("receiveUserId")))
            );
        } catch (Exception ex) {
            throw new WebQQDaoException(SQL_GET_MESSAGE_PAGE,ex);
        }

    }

    private static final String SQL_GET_MESSAGE_TIME_INTERVAL = " SELECT id,sendUserId,receiveUserId,content,sendDate,sendDeleteFlag,receiveDeleteFlag FROM message A  WHERE (A.sendUserId = ? AND A.receiveUserId = ?) OR (A.sendUserId = ? AND A.receiveUserId = ?) AND A.sendDate BETWEEN ? AND ? ORDER BY A.sendDate DESC  ";
    public List<Message> getMessage(User sendUser, User receiveUser, TimeInterval timeInterval) throws WebQQDaoException {
        try{
            Map<String,User> map = Maps.newHashMap();
            map.put(sendUser.getId(),sendUser);
            map.put(receiveUser.getId(),receiveUser);

            Object[] args = new Object[]{
                    sendUser.getId(),
                    receiveUser.getId(),
                    receiveUser.getId(),
                    sendUser.getId(),
                    timeInterval.getBeginDateTime(),
                    timeInterval.getEndDateTime()
            };
            int[] argTypes = new int[]{
                    Types.VARCHAR,
                    Types.VARCHAR,
                    Types.VARCHAR,
                    Types.VARCHAR,
                    Types.TIMESTAMP,
                    Types.TIMESTAMP
            };
            return jdbcTemplate.query(
                    SQL_GET_MESSAGE_TIME_INTERVAL,
                    args,
                    argTypes,
                    (rs, i) -> getMessageByResultSet(rs)
                            .setSendUser(map.get(rs.getString("sendUserId")))
                            .setReceiveUser(map.get(rs.getString("receiveUserId")))
            );
        } catch (Exception ex) {
            throw new WebQQDaoException(SQL_GET_MESSAGE_TIME_INTERVAL,ex);
        }

    }

    private Message getMessageByResultSet(ResultSet rs) throws SQLException{
        return new Message()
                .setId(rs.getString("id"))
                .setContent(rs.getString("content"))
                .setSendDate(rs.getTimestamp("sendDate"))
                .setSendDeleteFlag(rs.getInt("sendDeleteFlag"))
                .setReceiveDeleteFlag(rs.getInt("receiveDeleteFlag"));
    }

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
}
