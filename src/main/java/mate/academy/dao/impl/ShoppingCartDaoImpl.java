package mate.academy.dao.impl;

import java.util.Optional;
import mate.academy.dao.ShoppingCartDao;
import mate.academy.exception.DataProcessingException;
import mate.academy.lib.Dao;
import mate.academy.model.ShoppingCart;
import mate.academy.model.User;
import mate.academy.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

@Dao
public class ShoppingCartDaoImpl implements ShoppingCartDao {
    @Override
    public ShoppingCart add(ShoppingCart shoppingCart) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            User user = (User) session.merge(shoppingCart.getUser());
            shoppingCart.setUser(user);
            session.persist(shoppingCart);
            transaction.commit();
            return shoppingCart;
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't save the shopping cart " + shoppingCart, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Optional<ShoppingCart> getByUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ShoppingCart> getShoppingCartQuery =
                    session.createQuery("from ShoppingCart sc "
                                        + "left join fetch sc.user u "
                                        + "left join fetch sc.tickets t "
                                        + "left join fetch t.movieSession ms "
                                        + "left join fetch  ms.movie "
                                        + "left join fetch  ms.cinemaHall "
                                        + "where u = :user",
                            ShoppingCart.class);
            getShoppingCartQuery.setParameter("user", user);
            return getShoppingCartQuery.uniqueResultOptional();
        } catch (HibernateException e) {
            throw new DataProcessingException("Can't get shopping cart by user: " + user, e);
        }
    }

    @Override
    public void update(ShoppingCart shoppingCart) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.update(shoppingCart);
            transaction.commit();
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't update shopping cart " + shoppingCart, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}