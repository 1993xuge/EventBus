/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.greenrobot.eventbus;


/**
 * Posts events in background.
 * 同时实现了Runnable接口和Poster接口，与ThreadMode.ASYNC相对应。
 *
 * 它的作用是 通过enqueue()函数将事件保存到内部封装的PendingPostQueue队列中，
 * 并调用线程池执行该任务，在run()方法中将事件取出，并执行回调。
 * 
 * @author Markus
 */
class AsyncPoster implements Runnable, Poster {

    private final PendingPostQueue queue;
    private final EventBus eventBus;

    AsyncPoster(EventBus eventBus) {
        this.eventBus = eventBus;
        queue = new PendingPostQueue();
    }

    public void enqueue(Subscription subscription, Object event) {
        // 把 事件和Subscription对象 封装成 PendingPost
        PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
        // 将 生成的 pendingPost对象 加入到队列中
        queue.enqueue(pendingPost);
        eventBus.getExecutorService().execute(this);
    }

    @Override
    public void run() {
        PendingPost pendingPost = queue.poll();
        if(pendingPost == null) {
            throw new IllegalStateException("No pending post available");
        }
        // 从队列中取出 PendingPost对象
        eventBus.invokeSubscriber(pendingPost);
    }

}
