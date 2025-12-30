package com.my_server.tetrisserver;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    // 책임 : 데이터베이스 연결 소켓을 리턴함
    public static Connection getConnection() {
        String url = "jdbc:mysql://localhost:3306/store";
        String username = "root";
        String password = "bitc5600!";

        try {
            // new 클래스명(); 과 동일
            Class.forName("com.mysql.cj.jdbc.Driver");
            // conn =  프로토콜이 적용된 소켓
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("성공");
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
