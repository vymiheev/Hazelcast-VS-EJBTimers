package ru.grfc.crashtest.cluster;

import javax.naming.NamingException;
import java.io.Serializable;

/**
 * Created by mvj on 25.10.2017.
 */
public interface ILocalServiceResolver extends Serializable {
    <T> T lookupLocalServiceByClass(Class<T> clazz) throws NamingException;
}
