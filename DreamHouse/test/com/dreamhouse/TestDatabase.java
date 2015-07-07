package com.dreamhouse;

import com.dreamhouse.model.Game;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Created by jason on 2015/7/7.
 */
public class TestDatabase {

    @Test
    public void createTable(){
        //可以验证生成表是否正确
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("mysqlJPA");
        factory.close();
    }

    @Test
    public void test1() {
        save();
        update();
        find2();
    }

    public void save(){
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("mysqlJPA");
        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        Game game = new Game();
        game.setState(Game.STATE_WAITING);
        game.setName("my team");
        em.persist(game); //持久化实体
        em.getTransaction().commit();
        em.close();
        factory.close();
    }
    //new 、托管、脱管、删除

    public void update(){
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("mysqlJPA");
        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        Game game = em.find(Game.class, 1l);
        game.setName("hmk"); //person为托管状态
        em.getTransaction().commit();
        em.close();
        factory.close();
    }

    public void update2(){
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("mysqlJPA");
        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        Game game = em.find(Game.class, 1l);
        em.clear(); //把实体管理器中的所有实体变为脱管状态
        game.setName("hmk2");
        em.merge(game); //把脱管状态变为托管状态,merge可以自动选择insert or update 数据
        em.getTransaction().commit();
        em.close();
        factory.close();
    }

    public void remove(){
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("mysqlJPA");
        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        Game game = em.find(Game.class, 1l);
        em.remove(game); //删除实体
        em.getTransaction().commit();
        em.close();
        factory.close();
    }

    public void find(){
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("mysqlJPA");
        EntityManager em = factory.createEntityManager();
        Game game = em.find(Game.class, 2l); //类似于hibernate的get方法,没找到数据时，返回null
        System.out.println(game.getName());
        em.close();
        factory.close();
    }

    public void find2(){
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("mysqlJPA");
        EntityManager em = factory.createEntityManager();
        Game game = em.getReference(Game.class, 2l); //类似于hibernate的load方法,延迟加载.没相应数据时会出现异常
        System.out.println(game.getName()); //真正调用时才查找数据
        em.close();
        factory.close();
    }
}
