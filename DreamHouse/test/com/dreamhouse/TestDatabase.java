package com.dreamhouse;

import com.dreamhouse.model.Game;
import com.dreamhouse.model.Player;
import org.junit.Test;

import javax.persistence.*;
import java.util.List;

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
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("mysqlJPA");
        EntityManager em = factory.createEntityManager();
        save(em, createGame("my team"));
        save(em, createGame("my team1"));
        save(em, createGame("my team2"));
        save(em, createGame("my team3"));
        List<Game> list = findByName(em, "my team");
        System.out.println("findByName() name = my team, result size = " + list.size());
        List<Game> list2 = findAll(em);
        System.out.println("findAll() result size = " + list2.size());

        Game g = findByName(em, "my team").get(0);
        Player p = new Player();
        p.setPhone("136000000");
        g.addPlayer(p);
        save(em, g);

//        int removedSize = removeAll();
//        System.out.println("removeAll() size = " + removedSize);
        em.close();
        factory.close();
    }

    private Game createGame(String name) {
        Game game = new Game();
        game.setState(Game.STATE_WAITING);
        game.setName(name);
        return game;
    }

    public void save(EntityManager em, Game game) {
        em.getTransaction().begin();
        em.persist(game); //持久化实体
        em.getTransaction().commit();
    }

    public void updateName(EntityManager em, long id, String newName){
        em.getTransaction().begin();
        Game game = em.find(Game.class, id);
        if (game != null) {
            game.setName(newName); //person为托管状态
        }
        em.getTransaction().commit();
    }

    public void update2(EntityManager em, long id, String newName){
        em.getTransaction().begin();
        Game game = em.find(Game.class, id);
        em.clear(); //把实体管理器中的所有实体变为脱管状态
        game.setName(newName);
        em.merge(game); //把脱管状态变为托管状态,merge可以自动选择insert or update 数据
        em.getTransaction().commit();
    }

    public void remove(EntityManager em, long id) {
        em.getTransaction().begin();
        Game game = em.find(Game.class, id);
        em.remove(game); //删除实体
        em.getTransaction().commit();
    }

    public int removeAll(EntityManager em) {
        em.getTransaction().begin();// update/delete操作必须要transaction包着
        Query q = em.createQuery("DELETE FROM game");
        int removeSize = q.executeUpdate();
        em.getTransaction().commit();// update/delete操作必须要transaction包着
        return removeSize;
    }

    public Game find(EntityManager em, long id) {
        Game game = em.find(Game.class, id); //类似于hibernate的get方法,没找到数据时，返回null
        return game;
    }

    public Game find2(EntityManager em, long id) {
        Game game = em.getReference(Game.class, id); //类似于hibernate的load方法,延迟加载.没相应数据时会出现异常
        return game;
    }

    public List<Game> findByName(EntityManager em, String name) {
        TypedQuery<Game> q = em.createQuery("SELECT g FROM game g WHERE g.name = :nn", Game.class);
        q.setParameter("nn", name);
        return q.getResultList();
    }

    public List<Game> findAll(EntityManager em) {
        TypedQuery<Game> q = em.createQuery("SELECT g FROM game g", Game.class);
        List<Game> result = q.getResultList();
        return result;
    }
}
