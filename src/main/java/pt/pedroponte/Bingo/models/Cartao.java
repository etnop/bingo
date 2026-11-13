package pt.pedroponte.Bingo.models;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.IdGenerator;

public class Cartao {
    @Autowired
    private IdGenerator idGenerator;
    private String name;
    private int age;

    public Cartao(String name, int age) {
      
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

}
