package com.my_server.tetrisserver;

import com.my_server.tetrisserver.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TetrisRankRepository {

    public boolean insert(String name, int score) {
        String sql = "INSERT INTO tetris_score_tb(name, score) VALUES (?, ?)";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, name);
            pstmt.setInt(2, score);

            int result = pstmt.executeUpdate();
            return result == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RankDto> findTop(int limit) {
        String sql = """
                SELECT name, score
                FROM tetris_score_tb
                ORDER BY score DESC
                LIMIT ?
                """;

        List<RankDto> list = new ArrayList<>();

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new RankDto(
                            rs.getString("name"),
                            rs.getInt("score")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
