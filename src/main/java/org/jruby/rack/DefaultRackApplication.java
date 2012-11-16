/*
 * Copyright (c) 2010-2012 Engine Yard, Inc.
 * Copyright (c) 2007-2009 Sun Microsystems, Inc.
 * This source code is available under the MIT license.
 * See the file LICENSE.txt for details.
 */
package org.jruby.rack;

import org.jruby.Ruby;
import org.jruby.RubyObjectAdapter;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.IOException;

/**
 *
 * @author nicksieger
 */
public class DefaultRackApplication implements RackApplication {

    protected final RubyObjectAdapter adapter = JavaEmbedUtils.newObjectAdapter();
    private IRubyObject application;

    public DefaultRackApplication() {
    }

    public DefaultRackApplication(IRubyObject application) {
        setApplication(application);
    }

    public RackResponse call(final RackEnvironment env) {
        final Ruby runtime = getRuntime();
        try {
            RackInput io = new RackInput(runtime, env);
            try {
                IRubyObject servlet_env = JavaEmbedUtils.javaToRuby(runtime, env);
                adapter.setInstanceVariable(servlet_env, "@_io", io);
                IRubyObject response = __call(servlet_env);
                return (RackResponse) JavaEmbedUtils.rubyToJava(runtime, response, RackResponse.class);
            } finally {
                io.close();
            }
        } catch (IOException ex) {
            throw RaiseException.createNativeRaiseException(runtime, ex);
        }
    }

    public void init() { /* NOOP */ }

    public void destroy() { /* NOOP */ }

    public Ruby getRuntime() {
        return getApplication().getRuntime();
    }

    public IRubyObject getApplication() {
        if (application == null) {
            throw new IllegalStateException("no application set");
        }
        return application;
    }
    
    public void setApplication(IRubyObject application) {
        this.application = application;
    }

    public IRubyObject __call(final IRubyObject env) {
        return adapter.callMethod(getApplication(), "call", env);
    }
    
}
