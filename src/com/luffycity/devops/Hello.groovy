package com.luffycity.devops

/**
 * @author: YongxinLi
 * @mail: inspur_lyx@hotmail.com
 * @Date: 2020-04-13
 */

/**
 * @param content: what you want to show
 */
def hello(String content) {
    this.content = content
    return this
}

def sayHi() {
    echo 'Hi, ${this.content}'
    return this
}

def sayBye() {
    echo 'fuck off, get away of my side!'
    return this
}