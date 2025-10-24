package com.flow.eda.server.flow;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FlowMapper {

    @Select("SELECT * FROM flow_definition WHERE id=#{id}")
    Flow findById(String id);

    @Select(
            "<script>SELECT `id`,`name` FROM flow_definition WHERE id IN "
                    + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<Flow> findByIds(List<String> ids);

    @Select(
            "<script>SELECT * FROM flow_definition WHERE 1=1 "
                    + "<if test='username!=null'>AND `username`=#{username}</if>"
                    + "<if test='status!=null'>AND `status`=#{status}</if>"
                    + "<if test='name!=null'> AND `name` LIKE '%${name}%'</if>"
                    + " ORDER BY create_date DESC</script>")
    List<Flow> findByRequest(FlowRequest request);

    @Select("SELECT id FROM flow_definition WHERE username=#{username}")
    List<String> findIdsByUser(String username);

    @Insert(
            "INSERT INTO flow_definition (`id`,`name`,description,username,`status`,create_date,update_date) "
                    + "VALUES(#{id},#{name},#{description},#{username},#{status},#{createDate},#{updateDate})")
    void insert(Flow flow);

    @Update(
            "<script>UPDATE flow_definition SET <if test='name!=null'>`name`=#{name},</if>"
                    + "<if test='description!=null'>description=#{description},</if>"
                    + "update_date=#{updateDate} WHERE id=#{id}</script>")
    void update(Flow flow);

    @Delete(
            "<script>DELETE FROM flow_definition WHERE id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    void deleteByIds(List<String> ids);

    @Update("UPDATE flow_definition SET `status`=#{status} WHERE id=#{id}")
    void updateStatus(String id, String status);
}
