package com.luffycity.devops

/**
 * @author: YongxinLi
 * @mail: inspur_lyx@hotmail.com
 * @Date: 2020-04-13
 */

/**
 * @param content: what you want to show
 */
def Hello(String content) {
    this.content = content
    return this
}

def sayHi() {
    echo 'Hi, ${this.content}'
}

def sayBye() {
    echo 'fuck off, get away of my side!'
}